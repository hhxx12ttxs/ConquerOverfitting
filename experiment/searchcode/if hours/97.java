package homework_7;

public class Task {

	private String nameOfTask;
	private double workingHours;
	
	public void setnameOfTask(String name) {
		if(name != null) {
			this.nameOfTask = name;
		}
	}
	
	public void setworkingHours(double hours) {
		if(hours >= 0) {
			this.workingHours = hours;
		}
	}
	
	public String getnameOfTask() {
		return this.nameOfTask;
	}
	
	public double getworkingHours() {
		return this.workingHours;
	}
	
	Task(String name, double hours) {
		setnameOfTask(name);
		setworkingHours(hours);
	}
}

