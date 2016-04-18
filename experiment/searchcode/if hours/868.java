package Taks_1;

public class Task {

	private String name;
	private double workingHours;
	private Employee owner;

	public Employee getOwner() {
		return owner;
	}

	public void setOwner(Employee owner) {
		this.owner = owner;
	}

	public Task() {

		this.setName("Undefined");
		this.setWorkingHours(8);
	}
	
	public Task(String name, double workingHours) {

		this.setName(name);
		this.setWorkingHours(workingHours);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null || name.length() == 0) {
			return;
		}
		this.name = name;
	}

	public double getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(double workingHours) {
		if (workingHours < 0) {
			return;
		}
		this.workingHours = workingHours;
	}
	
	public void printInfo(){
		System.out.println("Name of task: "+this.getName());
		System.out.println("Hours left of the task: "+this.getWorkingHours());
		System.out.println();
	}

}

