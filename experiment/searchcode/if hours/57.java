package employeePart2Abstr;

public class Task {

	private String name;
	private int workingHours;

	Task(String name, int workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}

	void setName(String name) {
		if (name != null) {
			this.name = name;
		}
	}

	String getName() {
		return this.name;
	}

	void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			System.out.println("The working hours are not valid");
		}
	}

	int getWorkingHours() {
		return this.workingHours;
	}
}

