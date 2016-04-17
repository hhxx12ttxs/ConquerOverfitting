package ss.week1;

public class Employee {

	private int workHours; // Hours worked in the week.
	private double payRate; // Hourly pay rate in dollars.
	
	/**
	 * Method that calculates salary of an employee
	 *  based on how many hours worked. If more than
	 *   40 hours, then get paid for overtime.
	 */
	public double pay() {
		if (workHours <= 40) {
			return workHours * payRate;
		} else {
			return (40 * payRate) + (((workHours - 40) * 1.5) * payRate);
		}
	}
}

