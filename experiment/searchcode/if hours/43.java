public class Manager extends Worker
{
	Manager()
	{
		salary_for_hour = 4;
		profesion = "Manager";
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
		System.out.println("MANAGING");
	}
}

