package Homework9;

public class Task {
	private String name;
	private int workingHours;

	public Task(String name, int workingHours) {
		if (name != null && !name.isEmpty()) {
			this.name = name;
		}
		setWorkingHours(workingHours);
	}
	
	@Override
	public String toString() {
		return "Task name: " + name + "\nHours left to finish task: "
				+ workingHours;

	}
	
	public String getName() {
		return name;
	}
	
	public int getWorkingHours() {
		return workingHours;
	}
	
	public void setWorkingHours(int workingHours) {
		if(workingHours>=0) {
			this.workingHours = workingHours;
		}
	}
}

