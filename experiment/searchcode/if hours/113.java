
abstract class Worker 
{
	int hours_week;
	String name;
	String profesion;
	int hours;
	int salary_for_hour;
	int worked_hours;
	int fixed_salary;
	boolean available;

	Worker() 
	{
		name = "Worker";
		hours = 160;
		salary_for_hour = 1;
		worked_hours = 0;
		hours_week = 40;
		fixed_salary = 0;
		available = true;
	}
	int working_hours()
	{
		return 160 - hours;
	}
	int worker_salary()
	{
		return worked_hours * salary_for_hour + fixed_salary;
	}
	void working(){	}
	//When working hours are finished the employee becomes unavailable
	boolean available()
	{
		boolean result = true;
		if (0 == hours_week || available == false) 
		{
			result = false;
		}
		return result;
	}
}

