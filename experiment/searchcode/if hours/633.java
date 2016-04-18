public class HourlyEmployee extends Employee{
	private double wage;
	private int hours;

	/**
	* Constructor:<BR>
	* Creates an hourly employee w/ given attributes
	* @param firstName Employee first name
	* @param lastName Employee last name
	* @param ssn Employee social security number
	* @param wage Employee hourly wage
	* @param hours Employee's hours worked
	*/
	public HourlyEmployee(String firstName, String lastName, String ssn, double wage, int hours){
		super(firstName,lastName,ssn);
		setWage(wage);
		setHours(hours);
	}

	/**
	* Returns employee's hourly wage
	* @return employee's hourly wage
	*/
	public double getWage(){
		return wage;
	}

	/**
	* Returns hours employee has worked
	* @return hours employee has worked
	*/
	public int getHours(){
		return hours;
	}

	/**
	* Sets employee's hourly wage
	* @param wage employee's hourly wage
	*/
	public void setWage(double wage){
		if (wage < 0)
			throw new IllegalArgumentException("Wage cannot be negative");
		else
			this.wage = wage;
	}

	/**
	* Set hours employee has worked
	* @param hours Hours employee has worked
	*/
	public void setHours(int hours){
		if (hours < 0)
			throw new IllegalArgumentException("Hours cannot be negative");
		else
			this.hours = hours;
	}

	public String toString(){
		return super.toString() + "\nHourly Wage: $" + s.format(getWage()) + "\nHours: " + getHours();
	}

	/**
	* Returns employee's earnings
	* @return employee's earnings
	*/
	public double getEarnings(){
		return (hours > 40 ? wage * 40 + wage * (hours - 40) * 1.5 : wage * hours);
	}
}
