public class Designer extends Worker
{
	Designer()
	{
		salary_for_hour = 3;
		profesion = "Designer";
	}
	@Override
	public void working()
	{
		hours = 1;
		if(hours_week > 0)
		{
			hours_week -= hours;
			worked_hours += hours;
		}	
		System.out.println("CREATE DISIGNE");
	}
}

