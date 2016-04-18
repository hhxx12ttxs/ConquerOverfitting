package WorkDemo;

public class Task {
	
	private String taskName;
	private int workingHours;
	
	Task(String taskName, int workingHours) {
		setTaskName(taskName);
		setWorkingHours(workingHours);
	}
	
	public String getTaskName() {
		return taskName;
	}
	
	public void setTaskName(String taskName) {
		if (taskName.trim().isEmpty()) {
			System.out.println("You cannot input empty task name!");
			return;
		}
		this.taskName = taskName;
	}
	
	public int getWorkingHours() {
		return workingHours;
	}
	
	public void setWorkingHours(int workingHours) {
		if (workingHours < 0) {
			return;
		}
		this.workingHours = workingHours;
	}
	
}

