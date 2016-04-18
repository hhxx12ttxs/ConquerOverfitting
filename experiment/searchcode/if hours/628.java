package lessonfivefour;

public class Secretary implements Employee{
	private double numberOfHoursPermonth;
	private int numberOfyearsOfWorking;
	
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
	public String getEmployeeDetails(){
		return null;
	}
	public double calculateSalary(){
		if(numberOfyearsOfWorking>5){
			if(numberOfHoursPermonth>150){
				return (numberOfHoursPermonth*salaryPerHour+
						(numberOfHoursPermonth-150)*bonusPerHour)+((numberOfHoursPermonth*salaryPerHour+
								(numberOfHoursPermonth-150)*bonusPerHour)*0.1);
			}
			else{
				return (numberOfHoursPermonth*salaryPerHour)+
						(numberOfHoursPermonth*salaryPerHour)*0.1;
			}
		}
		else{
			return numberOfHoursPermonth*salaryPerHour;
		}
		
	}
}

