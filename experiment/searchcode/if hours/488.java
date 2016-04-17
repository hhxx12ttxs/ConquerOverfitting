package Impek.Gen;

public class Time implements Comparable<Time> {
	private int minutes;
	private int hours;
	
	public Time(String s) {
		parse(s);
	}
	
	public Time() {
		setHours(0);
		setMinutes(0);
	}
	
	public Time(int h, int m) {
		setHours(h);
		setMinutes(m);
	}

	public void parse(String data) {
		String[] v = data.split(":");
		if (v.length == 2) {
			minutes = Integer.parseInt(v[1]);
			hours = Integer.parseInt(v[0]);
		}
	}
	
	public Time substract(Time later) {
		Time result = later;
		if(result.getMinutes()<getMinutes())
			result.setHours((24 + result.getHours() - 1)%24);
		result.setHours(result.getHours() - getHours());
		result.setMinutes((result.getMinutes() + 60 - getMinutes()) % 60);
		return result;
	}
	
	public Time addition(Time second) {
		int minutes = getMinutes() + second.getMinutes();
		int hours = getHours() + second.getHours();
		if(minutes>=60)
			hours++;
		minutes %= 60;
		return new Time(hours,minutes);
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public int compareTo(Time another) {
		if(getHours()>another.getHours())
			return 1;
		else {
			if(getHours() == another.getHours()) {
				if(getMinutes() > another.getMinutes())
					return 1;
				else {
					if(getMinutes() == another.getMinutes())
						return 0;
					else
						return -1;
				}
			}
			else 
				return -1;
		}
	}
}

