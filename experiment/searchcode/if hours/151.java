package Task03;

public class Task {
	private String name;
	private int workingHours;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			System.out.println(name	+"working hours can`t be less than 0 \n");
		}
	}

	public Task(String name, int workingHours) {
		if (workingHours >= 0) {
			this.name = name;
			this.workingHours = workingHours;
		} else {
			System.out.println(name	+"working hours can`t be less than 0 \n");
		}
	}
}

