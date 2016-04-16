/*
 * Copyright 2011 Witoslaw Koczewsi <wi@koczewski.de>, Artjom Kochtchi
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
package ilarkesto.base.time;

import ilarkesto.base.Tm;

public final class TimePeriod extends ilarkesto.core.time.TimePeriod {

	public static final TimePeriod ZERO = new TimePeriod(0);

	private long millis;

	public TimePeriod(long millis) {
		super(millis);
	}

	public TimePeriod(String s) {
		super(s);
	}

	@Override
	public TimePeriod addDays(int days) {
		return new TimePeriod(millis + Tm.DAY);
	}

	@Override
	public TimePeriod multiplyBy(int factor) {
		return new TimePeriod(millis * factor);
	}

	public TimePeriod getPeriodTo(Time other) {
		return new TimePeriod(other.toMillis() - toMillis());
	}

	@Override
	public TimePeriod abs() {
		return millis < 0 ? new TimePeriod(-millis) : this;
	}

	public boolean isGreaterThen(TimePeriod other) {
		return millis > other.millis;
	}

	public boolean isLessThen(TimePeriod other) {
		return millis < other.millis;
	}

	public TimePeriod divide(int divisor) {
		return new TimePeriod(millis / divisor);
	}

	public TimePeriod add(TimePeriod tp) {
		return new TimePeriod(millis + tp.millis);
	}

	public TimePeriod subtract(TimePeriod tp) {
		return new TimePeriod(millis - tp.millis);
	}

	public TimePeriod subtract(Time time) {
		return new TimePeriod(millis - time.toMillis());
	}

	public TimePeriod multiplyBy(double factor) {
		return new TimePeriod(Math.round(millis * factor));
	}

	public String toHoursAndMinutesString() {
		long hours = toHours();
		long minutes = toMinutes() - (hours * 60);
		StringBuilder sb = new StringBuilder();
		sb.append(hours);
		sb.append(':');
		if (minutes < 10) sb.append('0');
		sb.append(minutes);
		return sb.toString();
	}

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

