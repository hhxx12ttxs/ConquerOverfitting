package lab4;
/**
 * @author Vaibhav
 */
public class FullTimeWorker extends Worker {
	private int hoursWorked;
	private static final int MAX_HOURS_ALLOWED = 240;
	
	public FullTimeWorker(String name, double salaryRate, int hoursWorked) {
		super(name, salaryRate);
		this.hoursWorked = hoursWorked;
	}
	
	/**
	 * Computes the pay, the FullTimeWorker instance is eligible for
	 * @return the pay recieved by the worker
	 */
	@Override
	public double computePay() {
		int hours;
		if(this.hoursWorked < MAX_HOURS_ALLOWED) {
			hours = this.hoursWorked;
		}
		else {
			hours = MAX_HOURS_ALLOWED;
		}
		
		return hours * this.getSalary();
	}
}

