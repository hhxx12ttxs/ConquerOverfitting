
public class HourlyWorker extends Worker {
	private String work;
	private double hours;
	
	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public double getHours() {
		return hours;
	}

	public void setHours(double hours) {
		this.hours = hours;
	}

	public HourlyWorker(String name, double hourSalary, String work, double hours) {
		super(name, hourSalary);
		setWork(work);
		setHours(hours);
	}

	double getWeeklySalary() {
		double sum = 0,lHours=getHours(),lHourSalary=getHourSalary();
		if(lHours>40){
			lHourSalary=lHourSalary*1.5;
			lHours=lHours-40;
			sum = lHours*lHourSalary;
			lHourSalary = lHourSalary/1.5;
			lHours=40;
		}
		sum=sum+(lHours*lHourSalary);
		return sum;
	}
}

