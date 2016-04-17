
public class employee {
	private int hours;
	private int hourly_rate;
	
	public void sethours(int nhours){
		this.hours=nhours;
	}
	
	public void sethourly_rate(int nhourly_rate){
		this.hourly_rate=nhourly_rate;
	}
	
	public int gethours(){
		return this.hours;
	}
	
	public int gethourly_rate(){
		return this.hourly_rate;
	}
	
	public double gross_pay(int hours, int hourly_rate){
		int a;
		a=hours-40;
		double pay1, pay2,tpay = 0;
		
		if(hours>40){
			pay1= 40*hourly_rate;
			pay2= a* hourly_rate * 0.5;
			tpay= pay1 + pay2;
		}
		
		else if(hours<40){
			tpay=hours*hourly_rate;
		}
		
		return tpay;	
	}
	
	
}

