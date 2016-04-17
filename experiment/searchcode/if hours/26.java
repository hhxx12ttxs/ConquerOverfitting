package model;

public class CompleteHours implements Comparable<CompleteHours> {
	private int hours;
	private int minutes;

	public CompleteHours() {
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	@Override
	public int compareTo(CompleteHours o) {
		if (this.hours == o.hours && this.minutes == o.minutes) {
			return 0;
		} else if (this.hours != o.hours) {
			return this.hours - o.hours;
		} else {
			return this.minutes - o.minutes;
		}
	}

	@Override
	public String toString() {
		return "CompleteHours [hours=" + hours + ", minutes=" + minutes + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hours;
		result = prime * result + minutes;
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
		CompleteHours other = (CompleteHours) obj;
		if (hours != other.hours)
			return false;
		if (minutes != other.minutes)
			return false;
		return true;
	}
	
}
