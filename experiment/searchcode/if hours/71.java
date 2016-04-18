package Encapsulation_references;

public class Task {

	private final String name;
	private double workingHours;

	
	Task(String name, double hours) {
		this.name = name;
		setWorkingHours(hours);
	}
	
	public String getName() {
		return this.name;
	}


	public double getWorkingHours() {
		return this.workingHours;
	}

	protected void setWorkingHours(double workingHours) {
		if(workingHours >= 0)
		this.workingHours = workingHours;
		else System.out.println("invalid working hours value");
	}
}

