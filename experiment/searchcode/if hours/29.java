public class Payroll
{
	private double hours;
	private double rate;
	public Payroll()
	{
		hours = 0;
		rate = 0;
	}
	public Payroll(double h, double r)
	{
		hours = h;
		rate = r;
	}
	public void setHours(double h)
	{
		hours = h;
	}
	public void setRate(double r)
	{
		rate = r;
	}
	public double getHours()
	{
		return hours;
	}
	public double getRate()
	{
		return rate;
	}
	public double getGrossPay()
	{
		double g;
		if(hours > 40)
		{
			g = (40 * rate) + ((40 - hours) * rate * 1.5);
		}
		else
		{
			g = hours * rate;
		}
		return g;
	}
}

