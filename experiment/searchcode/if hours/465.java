package opdrachtenNino.copy2;

public class Employee {
	private int hours;
	private double rate;
	
	public double pay(){
		if(hours <= 40){
			return hours * rate;
		}else{
			return 40 * rate + (hours - 40) * rate * 1.5;
		}
	}
}

