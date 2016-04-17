import tester.Validation;

public class HourlyWorker extends Employee {
	private double numHours;

	/**
	 * @param name
	 * @param ssn
	 * @param payRate
	 */
	public HourlyWorker(String name, String ssn, double payRate) {
		super(name, ssn, payRate);
		numHours = 0;
	}

	/**
	 * @return the numHours
	 */
	public double getNumHours() {
		return numHours;
	}

	@Override
	public double pay() {
		return numHours * getPayRate();
	}

	/**
	 * @param additionalHours
	 *            must be non-negative
	 */
	public void addHours(double additionalHours) {
		if (additionalHours < 0) {
			Validation.reportError("Invalid additional hours");
		} else {
			numHours += additionalHours;
		}
	}
	
	

}

