
public class FooCorporation {

	/**
	 * @param args
	 */
	public static double CalculateWage(double basePay, int hours){
		if(basePay < 8.00){
			System.out.println("Error! Base Pay Too Low");
			return -1;
		}
		
		if(hours > 60){
			System.out.println("Error! Hours Worked Too Long");
			return -1;
		}
		
		if(hours < 40){
			return basePay * hours;
		}else{
			int exceedHours = hours - 40;
			return exceedHours * hours * 1.5 + basePay * hours;
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println(CalculateWage(7.50, 35));
		System.out.println(CalculateWage(8.20, 47));
		System.out.println(CalculateWage(10.00, 73));
	}

}

