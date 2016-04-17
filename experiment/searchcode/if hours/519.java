package ss.week1;

public class Employee {
	private int hours; //hours worked in the week
	private double rate; //hourly pay rate (dollars)
	
	public Employee(int hours, double rate){
		this.hours = hours;
		this.rate = rate;
	}
	
	public static void main(String[] args){
		Employee Stijn = new Employee(40, 3.50);
		System.out.println(Stijn.pay());
	}
	public double pay(){
		if(hours <= 40){
			double loan = hours * rate;
		return loan;
		}
		else{
			double loan = 40 * rate + (hours - 40) * 1.5 * rate;
		return loan;
		}
		
	}
}

