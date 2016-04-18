
/**
 * Designer subclass of Employee
 *
 */
public class Design extends Employee{
	// Fields
	private final static Position p = Position.DESIGN;
	private double payRate;
	private double hours;
	
	/**
	 * Default constructor
	 */
	public Design()
	{
		super();
		payRate = 0;
		hours = 0;
	}
	
	/**
	 * Specialized constructor
	 * @param fName first name
	 * @param lName last name 
	 * @param empNum employee number
	 * @param payRate pay rate
	 * @param hours hours worked
	 */
	public Design(String fName, String lName, int empNum, double payRate, double hours)
	{
		super(fName, lName, empNum, p);
		setPayRate(payRate);
		setHours(hours);
		
	}
	/**
	 * Gets weekly pay for this designer
	 */
	public double calculateWeeklyPay() {
		
		if(hours <= 40)
			return getHours()*getPayRate();
		else if(hours < 0 || payRate < 0)
			return -1;
		else{
			double total;
			total = 40 * payRate;
			total += (hours - 40) * (payRate*1.5);
			return total;
		}
			
	}

	/**
	 * Get pay rate
	 * @return pay rate
	 */
	public double getPayRate() {
		return payRate;
	}

	/**
	 * Set pay rate
	 * @param payRate pay rate
	 */
	public void setPayRate(double payRate) {
		this.payRate = payRate;
	}

	/**
	 * Get hours worked
	 * @return hours worked
	 */
	public double getHours() {
		return hours;
	}
	
	/**
	 * Set hours worked
	 * @param hours hours worked
	 */
	public void setHours(double hours) {
		this.hours = hours;
	}
	
	/**
	 * Validates input by user
	 * @return false if either payRate or hours < 0.
	 */
	public boolean inputValidation()
	{
		if(payRate < 0 || hours < 0)
			return false;
		else
			return true;
	}

}

