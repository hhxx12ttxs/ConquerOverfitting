
public class PayScale {

	public static void payCalculator(double basePay, double hours){
		if(basePay < 8.00 || hours > 60.0){
			System.out.println("There is an error with the wage or hours entered");
		}else{
			double overtime = hours - 40.0;
			if(overtime > 0.0){
				double pay = (hours*basePay)+(overtime*(1.5*basePay));
				System.out.println(pay);
			}else{
				double pay = hours*basePay;
				System.out.println(pay);
			}
		}
	}
	
	
	public static void main(String[] args) {
		double basePay = 10.00;
		double hours = 38.0;
		
		payCalculator(basePay, hours);

	}

}

