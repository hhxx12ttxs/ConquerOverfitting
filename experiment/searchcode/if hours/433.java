package mitjava;

public class FooCorporation {
	public static void printpay(double bp, int hours) {
		if (bp < 8 || hours > 60) {
			System.out.print("error\n");
			return;
		}
		double pay = 0.0;
		if (hours > 40) {
			pay = (hours - 40) * (bp * 1.5);
			pay += 40 * bp;
		} 
		
		if (hours <=40) {
			pay = bp * hours;
		}
		System.out.print(pay+"\n");
	}
	public static void main(String args[]) {

		printpay(7.5, 35);
		printpay(8.2, 47);
		printpay(10.0, 73);
	}
}

