
/**
 * @author cjohns25
 *
 */
public class Employee 
{
	
	private String name;
	private double hourlyRate;
	private double hoursWorked;
	final static double OVERTIME_THRESHOLD = 40.0;
	
	
	public double computePaycheck()
	{
		if (hoursWorked <= OVERTIME_THRESHOLD)
		{
		  return hourlyRate * hoursWorked;
		}
		else
		{
			return (hourlyRate * OVERTIME_THRESHOLD) + 1.5 * (hourlyRate * (hoursWorked - 40));
		}
			
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the hourlyRate
	 */
	public double getHourlyRate() {
		return hourlyRate;
	}
	/**
	 * @param hourlyRate the hourlyRate to set
	 */
	public void setHourlyRate(double hourlyRate) {
		if (hourlyRate > 0)
		{
		 this.hourlyRate = hourlyRate;
		}
	}
	/**
	 * @return the hoursWorked
	 */
	public double getHoursWorked() {
		return hoursWorked;
	}
	/**
	 * @param hoursWorked the hoursWorked to set
	 */
	public void setHoursWorked(double hoursWorked) {
		if (hoursWorked > 0)
		{
		  this.hoursWorked = hoursWorked;
		}
	}
	

}

