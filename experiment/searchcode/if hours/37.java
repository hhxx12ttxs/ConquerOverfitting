/**
 * 
 * @author Shawn Vahid
 * @version Jun16.2015
 *
 */
public class HourlyEmployee extends Employee {

	private double hoursWorked;
	private static final double HOURLEY_WAGE = 50;
	
	/**
	 * 
	 * @param name
	 * @param hoursWorked
	 */
	public HourlyEmployee(String name, double hoursWorked) {
		super(name);
		setHoursWorked(hoursWorked);
	}

	@Override
	public double calculateMonthlyEarnings() {
		return hoursWorked * HOURLEY_WAGE;
	}

	/**
	 * 
	 * @return hoursWorked
	 */
	public double getHoursWorked() {
		return hoursWorked;
	}

	/*
	 * this method sets hours worked
	 */
	public void setHoursWorked(double hoursWorked) {
		if(hoursWorked >= 0){
			this.hoursWorked = hoursWorked;
		}
		else{
			this.hoursWorked = 0;
		}
	}

	@Override
	public String toString() {
		return "HourlyEmployee [hoursWorked=" + hoursWorked
				+ ", calculateMonthlyEarnings()=" + calculateMonthlyEarnings()
				+ ", toString()=" + super.toString() + "]";
	}
}

