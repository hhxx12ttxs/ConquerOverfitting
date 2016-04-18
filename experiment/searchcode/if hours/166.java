
public class Staff {
	
	private double rate, firstHours, secondHours, salary;
	
	public Staff(double payRate, double first, double second)
	{
		rate = payRate;
		firstHours = first;
		secondHours = second;
	}
	
	public double getSalary()
	{
		if (firstHours > 40)
		{
			salary = 40 * rate + (firstHours - 40) * rate * 1.5;
		}
		else
		{
			salary = firstHours * rate;
		}
		
		if (secondHours > 40)
		{
			salary += 40 * rate + (secondHours - 40) * rate * 1.5;
		}
		else
		{
			salary += secondHours * rate;
		}
		return salary;
	}

}

