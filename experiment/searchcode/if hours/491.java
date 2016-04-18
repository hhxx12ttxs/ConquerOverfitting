package work;

public class Task {
	private String task;
	private int workingHours;

	public String getTask() {
		return task;
	}

	public void setTask(String task) {
		if (task != null) {
			this.task = task;
		} else {
			System.out.println("Please enter a valid name for the task");
			setTask(task);
		}
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours > 0) {
			this.workingHours = workingHours;
		} else {
			System.out.println("Please enter a valid amount of working hours");
			setWorkingHours(workingHours);
		}
	}

}

