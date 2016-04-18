
public class Time implements Comparable<Time> {
	int hours;
	int minutes;
	
	public Time(int a , int b){
		hours = a;
		minutes = b;
		
	}
	public Time(){
	}
	@Override
	public int compareTo(Time o) {
		
		if (this.hours-o.hours!=0)
			return -(this.hours - o.hours);
		
		else 
			return -(this.minutes - o.minutes);
	}
}

