package employeeTask;

public class Task {

	private String name;
	private double workingHours;

	Task(String name, double workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name;
		}
	}

	public String getName() {
		return this.name;
	}

	public void setWorkingHours(double workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			System.out.println("The working hours are not valid");
		}
	}

	public double getWorkingHours() {
		return this.workingHours;
	}
}

