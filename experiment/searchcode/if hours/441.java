package home7;

public class Employee {

	private static final int MIN_WORKING_HOURS_LEFT = 0;
	private final String DEFAULT_EMPLOYEE_NAME = "Employee name";
	
	private final String name;
	private Task currentTask;
	private double hoursLeft;
	
	Employee(String name) {
		if (name != null && !name.equals("")) {
			this.name = name;
		}
		else {
			this.name = DEFAULT_EMPLOYEE_NAME;
		}
	}
	
	public void work() {
		if (this.hoursLeft >= this.currentTask.getWorkingHours()) {
			this.hoursLeft -= this.currentTask.getWorkingHours();
			this.currentTask.setWorkingHours(0);			
		}
		else {
			this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.hoursLeft);
			this.hoursLeft = MIN_WORKING_HOURS_LEFT;
		}
		
	}
	
	public void showReport() {
		System.out.println(this.name + " " + this.currentTask.getName() + " " 
				+ this.hoursLeft + " " + this.currentTask.getWorkingHours());
	}
	
	
	public String getName() {
		return name;
	}
	
	public Task getCurrentTask() {
		return currentTask;
	}
	public void setCurrentTask(Task currentTask) {
		if (currentTask != null && currentTask.getWorkingHours() > 0) {
			this.currentTask = currentTask;
		}
		else {
			System.out.println("This task is unavailable or it is finished");
		}
		
	}
	public double getHoursLeft() {
		return hoursLeft;
	}
	public void setHoursLeft(double hoursLeft) {
		if (hoursLeft > MIN_WORKING_HOURS_LEFT) {
			this.hoursLeft = hoursLeft;
		}
		else {
			this.hoursLeft = MIN_WORKING_HOURS_LEFT;
		}
		
	}
	
	
}

