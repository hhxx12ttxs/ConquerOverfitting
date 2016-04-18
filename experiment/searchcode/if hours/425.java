package ss.week1;

public class Employee {

	private static final int CONTRACT = 40;
	
	private int hours;
	private double rate;
	
	public double pay(){
		double pay = hours * rate;
		
		if(hours > CONTRACT)
			pay += (hours - CONTRACT) * rate/2;
		
		return pay;
	}
}

