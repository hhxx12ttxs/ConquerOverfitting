
public class Task {
	private String name;
	private double workingHours;	
	
	public String getName() {
		return this.name;
	}
	
	public Task(String name, double workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}
	
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Task name can not be null or empty!");
		}
		this.name = name;
	}
	
	public double getWorkingHours() {
		return this.workingHours;
	}
	
	public void setWorkingHours(double workingHours) {
		if (workingHours < 0) { // 0 in case the task is already finished
			throw new IllegalArgumentException("Working hours can not be negative!");
		}
		this.workingHours = workingHours;
	}
}
