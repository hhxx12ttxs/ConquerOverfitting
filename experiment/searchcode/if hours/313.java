package ss.week1;

public class Employee {
	// --------------------------------Instance variables
	private int hours; // Hours worked in the week
	private double rate; // hourly pay rate (dollars)
	public double paycheck; // amount of money the employee recieves
	public static final int REGULAR_HOURS=40;

	// --------------------------------Constructor
	public Employee() {
	}

	// --------------------------------Queries
	public double pay() {
		if (hours <= REGULAR_HOURS) {
			paycheck = hours * rate;
		} else if (hours > REGULAR_HOURS) {
			paycheck = (REGULAR_HOURS * rate) + ((hours - REGULAR_HOURS) * 1.5 * rate);
		} else {
			System.out.println("Enter a valid amount of hours");
		}
			return paycheck;
	}

	// --------------------------------Commands
	

	public static void main(String[] args) {

	}

}

