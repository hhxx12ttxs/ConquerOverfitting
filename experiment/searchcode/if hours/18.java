
public class Task {
	private String name;
	private int workingHours;

	public Task() {
		name = "Unnamed";
		workingHours = 0;
	}

	public Task(String name, int workHours) {
		if (name != null && !name.equals("")) {
			this.name = name;
		}

		if (workHours >= 0) {
			this.workingHours = workHours;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) {
			this.name = name;
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

