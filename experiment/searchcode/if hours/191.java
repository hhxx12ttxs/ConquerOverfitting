package Homework7_Encapsulation;

public class Task {
	private String name;
	private int workingHours;

	Task(String name, int workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}
	void printInfo() {
		System.out.println("Task name: " + name);
		System.out.println("Hours left to finish task: " + workingHours);
	}
	String getName() {
		return name;
	}
	int getWorkingHours() {
		return workingHours;
	}
	void setName(String name) {
		if (name != null && !name.isEmpty()) {
			this.name = name;
		}
	}
	void setWorkingHours(int workingHours) {
		if(workingHours>=0) {
			this.workingHours = workingHours;
		}
	}
}

