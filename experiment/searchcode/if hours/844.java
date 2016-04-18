package ss.week1;

public class Employee {
	private int hours;
	private double rate;
	public static final int WEEK = 40;
	public static final double OVERTIME_MULTIPLIER = 1.5;
	
	public static void main(String[] args){
		Employee obj1 = new Employee(50,2);
		System.out.println(obj1.pay());
		
		Employee obj2 = new Employee(30,2);
		System.out.println(obj2.pay());
	}
	
	public Employee(int hours, int rate) {
		this.hours = hours;
		this.rate = rate;
	}

	public double pay() {
		if (hours <= WEEK) {
			return hours * rate;
		} else {
			return (WEEK * rate) + ((hours - WEEK) * OVERTIME_MULTIPLIER * rate);
		}
	}
}

