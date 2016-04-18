package net.kloumpt;

public class Horaire {
	public final int hours, minutes;
	
	public Horaire(Horaire time){
		this(time.hours, time.minutes);
	}
	
	
	public Horaire(int hours, int minutes){
		if(hours>23 || hours<0) throw new IllegalArgumentException("Heures invalide");
		if(minutes>59 || minutes<0) throw new IllegalArgumentException("Minutes invalide");

		this.hours = hours;
		this.minutes = minutes;
	}
	
	public boolean apres(Horaire time){
		if(time==null){
			throw new IllegalArgumentException("Horaire null");
		}
		
		
		if(this.hours>time.hours){
			return true;
		} else if(this.hours==time.hours && this.minutes>time.minutes){
			return true;
		}
		return false;
	}
	
	public boolean equals(Object o){
		if(o==null || !(o instanceof Horaire)){
			return false;
		}
		return this.hours==((Horaire)o).hours && this.minutes==((Horaire)o).minutes;
	}
	
	public String toString(){
		return hours+":"+minutes;
	}
}

