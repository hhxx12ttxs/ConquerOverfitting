package cap10;

public class HourlyEmployee extends Employee{
	
	private double wage;
	private double hours;
	
	public HourlyEmployee(String firstName, String lastName,
			String socialSecurityNumber, double hourlyWage, double hoursWorked) {
		super(firstName, lastName, socialSecurityNumber);
		setWage(hourlyWage);
		setHours(hoursWorked);
	}

	@Override
	public double earnings() {
		// TODO Auto-generated method stub
		if (getHours() <= 40)
			return getWage() * getHours();
		else
			return 40*getWage() + (getHours() - 40)*getWage()*1.5;
	}

	public double getWage() {
		return wage;
	}

	public void setWage(double hourlyWage) {
		wage = (hourlyWage < 0) ? 0 : hourlyWage;
	}

	public double getHours() {
		return hours;
	}

	public void setHours(double hoursWorked) {
		hours = ((hoursWorked >= 0 ) && (hoursWorked < 168)) ? hoursWorked : 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return String.format("hourly Employee: %s\n%s: %f: %s: %f",
				super.toString(), "hourly wage", getWage(),
				"hourly worked", getHours());
	}
	
	

}

