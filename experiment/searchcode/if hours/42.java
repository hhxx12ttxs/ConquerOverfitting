public class Freelancer extends Worker
{
	Freelancer()
	{
		salary_for_hour = 3;
		profesion = "Freelancer";
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
		System.out.println("Freelancing");
	}	
}

