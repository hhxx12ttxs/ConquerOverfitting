package employee;

public class HourEmployee extends Employee {
	private double hourPay;
	private int hours;
	
	public HourEmployee() {
		super();
	}

	public HourEmployee(String name,int birthMonth,double hourPay,int hours) {
		super(name,birthMonth);
		this.hourPay=hourPay;
		this.hours=hours;
	}

	public double getSalary(int month){
		double res=super.getSalary(month);
		if(this.hours<160){
			res +=hourPay*hours;
		}else{
			res += hourPay*160 + hourPay*(hours-160)*1.5; 
		}
		return res;
		
	}
	
	
}

