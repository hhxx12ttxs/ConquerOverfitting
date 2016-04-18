
public class Task {
	private final int MIN_WORKING_HOURS = 0;

	private String name;
	private double workingHours;

	Task(String newName, double newWorkingHours) {
		setName(newName);
		setHours(newWorkingHours);
	}

	private void setName(String newName) {
		if (newName != null && !newName.equals("")) {
			this.name = newName;
		}
	}

	public void setHours(double newWorkingHours) {
		if (newWorkingHours >= MIN_WORKING_HOURS) {
			this.workingHours = newWorkingHours;
		}
	}

	public double getWorkingHours() {
		return this.workingHours;
	}

	public String getName() {
		return this.name;
	}
}

