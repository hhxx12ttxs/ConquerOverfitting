package com.orangeandbronze.schoolRegistration;

public class Schedule {
	private final Days days;
	private final Hours hours;
	
	public Schedule(Days days, Hours hours){
		this.days = days;
		this.hours = hours;
	}

	public Days getDays() {
		return this.days;
	}

	public Hours getHours() {
		return this.hours;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((days == null) ? 0 : days.hashCode());
		result = prime * result + ((hours == null) ? 0 : hours.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Schedule other = (Schedule) obj;
		if (days != other.days)
			return false;
		if (hours != other.hours)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return days + " " + hours;
	}
}

