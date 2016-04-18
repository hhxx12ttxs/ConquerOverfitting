package ps2;

class FooCorparation {
	public static void main (String[] args){
		System.out.println(payment(41));
	}
	public static double payment(int hours) {
		double payment = 0.0;
		double basePay = 8;
		int normalHours = 0;
		int extraHours = 0;
		if (hours > 60) {
			normalHours = 40;
			extraHours = 20;
		} else if (hours > 40) {
			normalHours = 40;
			extraHours = hours - 40;
		} else {
			normalHours = hours;
		}
		payment = basePay * normalHours + extraHours * 1.5 *basePay;
		return payment;
	}
}

