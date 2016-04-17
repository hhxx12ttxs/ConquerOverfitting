public class worker
{
	private double hours;
	private double rate;

	public worker(double a, double b)
	{
		hours = a;
		rate = b;
	}

	public double getPay()
	{
		double pay;

		if (hours <= 40)
			pay = hours * rate;

		if (hours > 40)
			pay = (1.5 * hours) * rate;

		return pay;
	}
}
