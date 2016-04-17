
public class PayRoll {

	final private double standardHours = 40d; 
	final private double rateIncrease = 50d;
	
	public double calculatePayRoll(double workedHours, double hourlyRate){
		Double payroll = Double.NaN;
		if (workedHours <= 0 || hourlyRate <= 0)
			payroll = 0.0;
		else{
			boolean rateIncreased = (workedHours > standardHours) ? true : false;
			if(rateIncreased){
				double overtime = workedHours - standardHours;
				payroll = (standardHours * hourlyRate) + (overtime * hourlyRate * (1 + rateIncrease/100d)); 
			}
			else
				payroll = workedHours * hourlyRate;				
		}
		return payroll.doubleValue();
	}
	
	public String getPayRoll(double workedHours, double hourlyRate){
		String msj = new String();
		if (workedHours <= 0d)
			msj = "you have to introduce a valid hours worked";
		else if (hourlyRate <= 0d)
			msj = "you have to introduce a valid hourly rate";
		else
			msj = String.format("You worked %.2f hours and you payroll is %.2f", workedHours, calculatePayRoll(workedHours, hourlyRate));			
		return msj;
	}		
}

