package office;

public class Task {

	private String name;
	private int workingHours;

	public Task(String name, int workingHours){
		this.name = name;
		setWorkingHours(workingHours);
		
	}

	public int getWorkingHours() {
		return workingHours;
	}

	void setWorkingHours(int workingHours) {
		if(workingHours >= 0)
			this.workingHours = workingHours;
	}

	public String getName() {
		return name;
	}
}

