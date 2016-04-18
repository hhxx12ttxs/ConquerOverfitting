/*
Caleb Thomas
4/9/14
AP Computer Science
 */
package Assignment1;

public class HourlyWorker extends Worker {

	//Instance Fields
	private int hours;
	
	public HourlyWorker(String name, double wage,int hours) {
		super(name,wage);
		this.hours = hours;
		// TODO Auto-generated constructor stub
	}
	
	public double wage(){
		
		if(hours > 40)
			return (hours - 40)*(super.getWage() * 1.5) + this.computePay(40);
		else if(hours > 0)
			return this.computePay(hours);
		else return 0;
	}


}

