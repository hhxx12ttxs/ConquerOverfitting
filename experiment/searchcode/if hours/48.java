package ss.week1;

public class Employee {

	private int hours;
	private double rate;
	
	double salary(){
		if (hours <= 40){
			return hours * rate;
		} else {
			return 40 * rate + (hours-40) * 0.5 * rate;
		}
	}
	
}

