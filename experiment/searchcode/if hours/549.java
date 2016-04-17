package paycheck.practice;

public class Paycheck {
	
	private double rate;
	public double hours;
	
	public Paycheck(double payRate){
		rate = payRate;
	}

	public void setHours(double hour){
		hours = hour;
	}
	
	public double calcPay(){
		
		if(hours<=40) return rate * hours;
		
		else return (rate*40) + ((rate*1.5)*(hours-40));
	}
	
	public String toString(){
		return "This week you earned $" + calcPay();
	}
}

