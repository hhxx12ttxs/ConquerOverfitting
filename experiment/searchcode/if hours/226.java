package gem.day6.homework;

public class HourlyEmployees extends Employee
{
	public HourlyEmployees(String name, int month, double hourlySalary, int hours) {
		super(name, month);
		this.hourlySalary = hourlySalary;
		this.hoursMonth = hours;
	}

	private double hourlySalary;
	private int hoursMonth;
	public double getHourlySalary() {
		return hourlySalary;
	}
	public void setHourlySalary(double hourlySalary) {
		this.hourlySalary = hourlySalary;
	}
	public int getHours() {
		return hoursMonth;
	}
	public void setHours(int hours) {
		this.hoursMonth = hours;
	}

	
	@Override
	public double getSalary ()
	{
		if (hoursMonth >160) {
			return (hoursMonth-160)*1.5*hourlySalary +160*hourlySalary;
			
		}
		else {
			return hourlySalary*hoursMonth;
		}
		
	}
	
	
}

