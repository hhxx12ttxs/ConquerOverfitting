
public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;
	
	private void showReport(){
		System.out.println( this.name + " after working on " + this.currentTask.getName() +
				" has left " + this.hoursLeft + " hours and has to work " 
				+ this.currentTask.getWorkingHours() + " more hours to complete it.");
	}
	public Employee() {
		name = "Unnamed";
		hoursLeft = 8;
		currentTask = new Task("Chilling", 0);
	}

	public Employee(String name) {
		this();
		if (name != null && !name.equals("")) {
			this.name = name;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) {
			this.name = name;
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		if (currentTask != null) {
			this.currentTask = currentTask;
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		}
	}
	
	public void work(){
		if( this.hoursLeft > this.currentTask.getWorkingHours() ){
			this.hoursLeft -= this.currentTask.getWorkingHours();
			this.currentTask.setWorkingHours(0);;
		}
		else{
			this.currentTask.setWorkingHours(this.currentTask.getWorkingHours()
					- this.hoursLeft);
			this.hoursLeft = 0;
		}
		showReport();
	}
	
}

