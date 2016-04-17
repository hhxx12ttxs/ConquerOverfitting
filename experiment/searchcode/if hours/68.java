
public class Task {
	private String name;
	private int workingHours;

	Task(String name, int workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) {
			this.name = name;
		} else {
			this.name = "Default";
		}
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			this.workingHours = 0;
		}
	}

}

