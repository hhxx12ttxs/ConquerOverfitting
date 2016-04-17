
public class HourlyEmployee extends Employee{

	private double wage;
	private double hours;
	
	public HourlyEmployee(String first, String last, String ssn,double hourlyWage,double hoursWorked)
	{
		super(first,last,ssn);
		setWage(hourlyWage);
		setHours(hoursWorked);
	}
	
	public void setWage(double hourlyWage)
	{
		wage = (hourlyWage < 0.0) ? 0.0 : hourlyWage;
	}
	
	public double getWage()
	{
		return wage;
	}
	
	public void setHours(double hoursWorked)
	{
		hours = ( (hoursWorked >= 0.0) && (hoursWorked <= 168.0)) ? hoursWorked : 0.0;
	}
	
	public double getHours()
	{
		return hours;
	}
	
	//upologizei tis apodoxes kai epikaluptei tin abstract me9odo earnings stin Employee
	@Override
	public double earnings()
	{
		if(getHours() <= 40) //xwris uperwries
			return getWage() * getHours();
		else
			return 40 * getWage() + (getHours() - 40) * getWage() * 1.5;
	}
	
	//returning string format of HourlyEmployee object
	@Override
	public String toString()
	{
		return String.format("hourly employee: %s\n%s: $%,.2f; %s %,.2f",
				super.toString(),"hourly wage",getWage(),"hours worked",getHours());
	}
}


