
public class Worker extends Human implements IWorker {
	private double weekSalary;
	private double workHoursPerDay;
	
	public Worker(String fName, String lName, double weekSalary, double workHours) {
		super(fName, lName);
		this.weekSalary = weekSalary;
		this.workHoursPerDay = workHours;
	}

	public double getWeekSalary() {
		return weekSalary;
	}

	public void setWeekSalary(double weekSalary) {
		this.weekSalary = weekSalary;
	}

	public double getWorkHoursPerDay() {
		return workHoursPerDay;
	}

	public void setWorkHoursPerDay(double workHoursPerDay) {
		if (workHoursPerDay < 2 || workHoursPerDay > 14) {
			throw new IllegalArgumentException();
		}
		this.workHoursPerDay = workHoursPerDay;
	}

	public double moneyPerHour() {
		return (getWeekSalary() / 5) / getWorkHoursPerDay();
	}

	@Override
	public String work() {
		String result = getfName() + " " + getlName() + " works " 
				+ getWorkHoursPerDay() + " hours per day and makes "
				+ getWeekSalary() * 4 + "leva a month.";
		return result;
	}

}

