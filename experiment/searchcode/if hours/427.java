package lessonfivefour;

public class Manager implements Employee{
	private double numberOfHoursPermonth;
	private int numberOfyearsOfWorking;	
	private double extraHours;
	
	public double getNumberOfHoursPermonth() {
		return numberOfHoursPermonth;
	}
	public void setNumberOfHoursPermonth(double numberOfHoursPermonth) {
		this.numberOfHoursPermonth = numberOfHoursPermonth;
	}
	public int getNumberOfyearsOfWorking() {
		return numberOfyearsOfWorking;
	}
	public void setNumberOfyearsOfWorking(int numberOfyearsOfWorking) {
		this.numberOfyearsOfWorking = numberOfyearsOfWorking;
	}
	public double getExtraHours() {
		return extraHours;
	}
	public void setExtraHours(double extraHours) {
		this.extraHours = extraHours;
	}
	public String getEmployeeDetails(){
		return null;
	}
	public double calculateSalary(){		
		if(numberOfHoursPermonth>150){
			return (salaryPerHour*(150*2))+(overTimePerHour*((numberOfHoursPermonth-150)*2));
		}
		else{
			return (salaryPerHour*numberOfHoursPermonth*2);
		}
	}
}

