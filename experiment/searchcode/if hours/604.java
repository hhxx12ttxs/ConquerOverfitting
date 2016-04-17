package db;

import db.interfaces.IRecord;

public class Record implements IRecord{
	
	private int hours;
	private int minutes;
	private int seconds;
	
	public Record(){
		this(0,0,0);
	}
	public Record(int hours, int minutes, int seconds){
		this.setHours(hours);
		this.setMinutes(minutes);
		this.setSeconds(seconds);
	}

	@Override
	public int getHours() {
		return this.hours;
	}

	@Override
	public void setHours(int h) {
		this.hours = h;
		
	}

	@Override
	public int getMinutes() {
		return this.minutes;
	}

	@Override
	public void setMinutes(int m) {
		this.minutes = m;
		
	}

	@Override
	public int getSeconds() {
		return this.seconds;
	}

	@Override
	public void setSeconds(int s) {
		this.seconds = s;
	}
	@Override
	public int compareTo(IRecord r) {
		int output = -1;
		if(this.hours > r.getHours()){
			output = 1;
		}
		else if(this.hours == r.getHours()){
			if(this.minutes > r.getMinutes()){
				output = 1;
			}
			else if(this.minutes == r.getMinutes()){
				if(this.seconds > r.getSeconds()){
					output = 1;
				}
				else if(this.seconds == r.getSeconds()){
					output = 0;
				}
				else if(this.seconds < r.getSeconds()){
					output = -1;
				}
			}
			else if(this.minutes < r.getMinutes()){
				output = -1;
			}
		}
		else if(this.hours < r.getHours()){
			output = -1;
		}
		return output;
	}
}

