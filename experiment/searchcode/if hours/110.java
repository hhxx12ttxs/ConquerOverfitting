package lesson08;

public class Task {
	private String name;
	private int workingHours;
	
	public Task(String name, int workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if(name != null) {
			this.name = name;
		}else {
			this.name = "";
		}
	}
	
	public int getWorkingHours() {
		return workingHours;
	}
	
	public void setWorkingHours(int workingHours) {
		if(workingHours >= 0) {
			this.workingHours = workingHours;
		}
	}
}

