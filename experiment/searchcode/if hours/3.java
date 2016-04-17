
public class Task {
	private String name;
	private int workingHours;
	
	Task(String name, int workingHours){
		if(name != null){
			this.name = name;
		}
		if(workingHours > 0){
			this.workingHours = workingHours;
		}
	}
	
	public String getName() {
		return name;
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if(workingHours > 0){
		this.workingHours = workingHours;
		}
	}

}

