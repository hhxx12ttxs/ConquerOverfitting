package p13_6;

public class HourlyWorker extends Worker{
	public HourlyWorker(String n, double s){
		super(n, s);
	}
	
	public double computePay(int hours){
		if(hours >= 40)
			return hours * getHourlySalary();
		else
			return hours*getHourlySalary() + ((hours-40)/1.5) * getHourlySalary();
	}
}

