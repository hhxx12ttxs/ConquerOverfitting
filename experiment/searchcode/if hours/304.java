
public class Task {

	private String name;
	private int workingHours;

	public Task() {
		name = "Task with no name";
		workingHours = 1;
	}

	public Task(String name, int workingHours) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = "Task with no name";
		}
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			workingHours = 0;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = "Task with no name";
		}
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			workingHours = 0;
		}
	}

	public void printInfo() {
		System.out.println("Task name: " + this.name);
		System.out.println("Task working hours: " + this.workingHours);
	}

}

