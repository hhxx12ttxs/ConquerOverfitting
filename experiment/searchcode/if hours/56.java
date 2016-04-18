public class Task {
	private String name;
	private int workingHours;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null)
			this.name = name;
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0)
			this.workingHours = workingHours;
	}

	public Task() {
		this.name = "RandomTask";
		this.workingHours = 7;
	}

}

