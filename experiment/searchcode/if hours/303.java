public class HourlyEmployee extends Employee{
	private double hourlyRate;
	private double weeklyHours;

	public HourlyEmployee(String name, double rate) {
		super(name);
		this.hourlyRate = rate;
		
		this.weeklyHours = 0;
	}

	public void setHourlyRate(double rate) {
		this.hourlyRate = rate;
	}

	// These calculations are pretty cheap. I don't think I need to store the weeklyPay.
	@Override
	public double getPaycheck() {
		if (weeklyHours > 40.0){
			return (40 * hourlyRate + (weeklyHours - 40) * 1.5 * hourlyRate);
		} else {
			return this.hourlyRate * weeklyHours;
		}
	}

	// Adds the hours from a shift to the weeklyHours member
	public void clockIn(double hours){
		weeklyHours += hours;
	}

	// For a new week, or if a mistake was made clocking in
	public void setHours(double hours){
		weeklyHours = hours;
	}

	public double getHours(){
		return weeklyHours;
	}

	@Override
	public String toString() {
		String ret = "HourlyEmployee " + name + " (ID = " + ID + ", hourlyRate = " + hourlyRate + ", weeklyHours = " + weeklyHours + ")";
		return ret;
	}
}

