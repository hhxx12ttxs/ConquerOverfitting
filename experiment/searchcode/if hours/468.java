package less.lesson08;

public class Task {
	private String name;
	private double workingHours;
	
	public Task(String name, double workingHours) {
		this.name = name;
		this.workingHours = workingHours;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name != null && name.length() > 1) {
			this.name = name;
		} else {
			System.out.println("Invalid name");
		}
	}

	public double getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(double workingHours) {
		if(workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			System.out.println("Invalid value");
		}
	}
	
}

