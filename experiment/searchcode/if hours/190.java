package test;

public class Employee {

	private static int hours;
	private static double rate;
	public static final int REGULAR_HOURS = 40;
	public static final double OVERTIME_RATE = 1.5;
	
	public static double pay(){
		if(hours > REGULAR_HOURS){
			return  (rate*REGULAR_HOURS)+(rate*OVERTIME_RATE*(hours-REGULAR_HOURS));
		}
		else{
			return  rate*hours;
		}
	}
	
	public static void main(String[] args){
		hours = Integer.parseInt(args[0]);
		rate = Double.parseDouble(args[1]);
		System.out.println("Expected: 192.5");
		System.out.println("testStand:" + pay());
		System.out.println(hours + rate);
	}
	
}

	
