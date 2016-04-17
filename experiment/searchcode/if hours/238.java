package bjc.payroll;

// Set of hours worked by an employee
public class PayHours {
	// Number of regular hours worked
	private int hours;
	
	// Number of overtime hours worked
	private int overtime;

	// Do-nothing constructor
	public PayHours() {
	}

	// Constructor for employees who work no overtime
	public PayHours(int hours) {
		super();
		this.hours = hours;
	}


	// Fully initializing constructor
	public PayHours(int hours, int overtime) {
		this.hours = hours;
		this.overtime = overtime;
	}

	// Get the total number of hours worked
	public int getTotalHours() {
		return overtime + hours;
	}
	// Get the hours of overtime worked
	public int getOvertime() {
		return overtime;
	}

	// Get the regular hours worked
	public int getHours() {
		return hours;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + hours;
		result = prime * result + overtime;
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PayHours other = (PayHours) obj;
		if (hours != other.hours)
			return false;
		if (overtime != other.overtime)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Worked Hours: " + hours + "\n" + 
				(overtime != 0 ? "Overtime: " + overtime : "");
	}
}

