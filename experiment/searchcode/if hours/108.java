package home7;

public class Task {
	
	
	private static final String DEFAULT_TASK_NAME = "Task";
	private static final int MIN_WORKING_HOURS = 0;
	
	private String name;
	private double workingHours;
	
	public Task(String name, double workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if (name != null && !name.equals("")) {
			this.name = name;
		}
		else {
			this.name = DEFAULT_TASK_NAME;
		}
		
	}
	public double getWorkingHours() {
		return workingHours;
	}
	public void setWorkingHours(double workingHours) {
		if (workingHours > MIN_WORKING_HOURS) {
			this.workingHours = workingHours;
		}
		else {
			this.workingHours = MIN_WORKING_HOURS;
		}
		
	}
	

}

