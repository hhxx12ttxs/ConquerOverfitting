package EmplyeeTaks;

public class Task {

	private String name;
	private int workingHours;

	Task() {
		name = "";
		workingHours = 0;
	}

	Task(String name, int workingHours) {
		this();
		if (name != null && workingHours > 0) {
			this.name = name.trim();
			this.workingHours = workingHours;
		}
	}

	public String getName() {
		if (name != null) {
			return name;
		}
		return "";
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name.trim();
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

