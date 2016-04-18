package ss.week1;

public class Employee {
	private int hours;
	private double rate;
	private static final int MAXHOURS = 40;
	
	public void setRate(double factor)
	{
		this.rate = factor;
	}
	
	public void setHours(int amount)
	{
		this.hours = amount;
	}
	
	public double pay()
	{
		if(hours<MAXHOURS)
		{
			return hours * rate;
		}
		else
		{
			return (MAXHOURS * rate) + ((hours - MAXHOURS) * (rate * 1.5));
		}
	}
}

