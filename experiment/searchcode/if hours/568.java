
public class calculatePay {

	public static void main(String[] args) {
		//TODO 
		int hours = 55;
		double wage = 8.0;
        calculate(hours, wage);
	}
	
	public static void calculate(int hours, double wage) {
	double overtime = 1.5*wage;
	int overtimehours = hours-40;
	double overtimepay = overtimehours*overtime;
	double regularpay = 40*wage;
	double salary = regularpay+overtimepay;
	if ((hours<40)||(hours>60)) {
		System.out.println("You are fired");
	}
	else {
		System.out.println(salary);
	}
	}
}




