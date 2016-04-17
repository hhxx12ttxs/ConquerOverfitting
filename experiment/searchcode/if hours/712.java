package homework;

public class Task {
	private String name;
	private float workingHours;
	private Employee assignedTo;
	
	public Task(String name,float f){
		if(name==null){
			this.name="Unnamed";
		}
		else {
			this.name=name;
		}
		this.setWorkingHours(f);
		this.setAssignedTo(null);
	}
	
	public String getName() {
		return name;
	}
	
	public float getWorkingHours() {
		return workingHours;
	}
	
	public void setAssignedTo(Employee assignedTo) {
		this.assignedTo = assignedTo;
	}
	
	public Employee getAssignedTo() {
		return assignedTo;
	}
	
	public void setWorkingHours(float hours) {
		if(hours<0){
			this.workingHours=0;
		}
		else{
			this.workingHours=hours;
		}
	}

}

