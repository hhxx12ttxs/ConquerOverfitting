package office;

public class Task {
	private static final int WORKING_HOURS_TO_SET_IN_CASE_OF_WRONG_INPUT = 0;
	private final String name;
	private int workingHours;

	Task(String name, int workingHours) {
		if (!(name.equals(""))) {
			this.name = name;
		} else {
			this.name = "Miracle";
		}
		setWorkingHours(workingHours);
	}

	String getName() {
		return name;
	}

	int getWorkingHours() {
		return workingHours;
	}

	void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			this.workingHours = WORKING_HOURS_TO_SET_IN_CASE_OF_WRONG_INPUT;
			System.out.println(this.name + "-wrong input for working hours");
		}
	}

}

