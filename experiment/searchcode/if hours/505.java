package ss.week1;

public class Employee {

	private int hours; // hours worked in the week
	private double rate; // hourly pay rate (dollars)
	private static final int WORKWEEK = 40;

	public Employee() {
		hours = 50;
		rate = 10;
	}

	public double pay() {
		if (hours <= WORKWEEK) {
			return hours * rate;
		}
		return WORKWEEK * rate + (hours - WORKWEEK) * (rate * 1.5);
	}
}

