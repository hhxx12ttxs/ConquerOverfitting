

public class Task {

	private String taskName;
	private int workingHours;

	public Task(final String taskName, int workingHours) {
		this.setTaskName(taskName);
		this.setWorkingHours(workingHours);
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		if (taskName != null) { 
			this.taskName = taskName.trim();
		} else {
			System.out.println("Task name can't be null! Task name set to \"Unknown\"");
			this.taskName = "Unknown";
		}
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		}
	}

}

