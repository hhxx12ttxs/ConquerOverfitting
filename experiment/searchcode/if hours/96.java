package Aula7;

import java.io.Serializable;

public class Hour implements Serializable{
	private static final long serialVersionUID = 4L;
	private int hours;
	private int minutes;
	
	public Hour(int hours, int minutes) {
		assert validate(hours,minutes);
		this.hours = hours;
		this.minutes = minutes;
	}
	
	private boolean validate(int hours,int minutes){
		if(hours>=0 && hours<24){
			if(minutes>=0 && minutes <=59)
				return true;
			else
				return false;
		}else if (hours==24){
			if (minutes==0)
				return true;
			else
				return false;
		}
		return false;
	}
	
	public int getHours(){
		return hours;
	}
	
	public int getMinutes(){
		return minutes;
	}
	
	public Hour sumHours(Hour h){
		int sumHours= this.hours + h.getHours();
		int sumMinutes= this.minutes + h.minutes;
		
		if(sumHours>24){
			sumHours= sumHours-24;
		}else if (sumHours== 24 && sumMinutes>0){
			sumHours=0;
		}
		if(sumMinutes>60){
			sumHours+=(int)(sumMinutes/60);
			sumMinutes-=60;
		}
			
		return new Hour(sumHours,sumMinutes);
	}
	
	public int toSeconds(){
		return this.hours*360+this.minutes*60;
	}
	
	public static Hour toHours(int seconds){
		int hours = (int)(seconds/360);
		int minutes= seconds - (hours*360);
		minutes= (int)(minutes/60);
		return new Hour(hours,minutes);
	}
	
	public String toString(){
		if(hours<10 && minutes<10)
			return "0"+hours+":0"+minutes;
		else if(hours<10)
			return "0"+hours+":"+minutes;
		else if(minutes<10)
			return hours+":0"+minutes;
		return hours+":"+minutes;
	}
	
	
}

