package employee;

public class Task {
	private String name;
	// name bih go napravil final i da nqma setter, no po zadanie
	// pishe da se napravqt setters i getters za poletata
	private int workingHours;

	Task(String name, int workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}

	String getName() {
		return name;
	}

	void setName(String name) {
		if (!(name.equals(""))) {
			this.name = name;
		} else {
			this.name = "Set name to this job again!";
		}
	}

	int getWorkingHours() {
		return workingHours;
	}

	void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			this.workingHours = 0;
			System.out.println(this.name + "-wrong input for working hours");
		}
	}

}

