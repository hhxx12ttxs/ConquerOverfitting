package Encapsulation_Homework;

public class Employee {

	private String name;
	Task currentTask;
	private int hoursLeft;

	

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && name != "") {
			this.name = name;
		} else {
			this.name = "The name is wrong";
		}
	}
	
	Employee(String name) {
		setName(name);
	}
	
	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}
	public int getHoursLeft() {
		return hoursLeft;
	}
	
	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		} else {
			this.hoursLeft = -9999;   // umishleno e taka za da napravi vpechatlenie che e greshno
		}
	}

	void work() {
		if (this.hoursLeft > this.getCurrentTask().getWorkingHours()) {
			this.hoursLeft = (this.hoursLeft - this.currentTask.getWorkingHours());
			this.currentTask.setWorkingHours(0);
			return;
		}
		if (this.hoursLeft < this.getCurrentTask().getWorkingHours()){
			
			this.getCurrentTask().setWorkingHours((this.hoursLeft - this.getCurrentTask().getWorkingHours()) * -1);
			this.hoursLeft = 0;
			return;
		}
		
		if (this.hoursLeft == this.getCurrentTask().getWorkingHours()) {
			this.hoursLeft = 0;
			this.getCurrentTask().setWorkingHours(0);
			return;
		}
	}
	
	void showReport(){
		work();
		System.out.println("Name of employee: " + this.getName());
		System.out.println("Task of the employee: " + this.getCurrentTask().getName());
		System.out.println("Working hours left: " + this.getHoursLeft());
		System.out.println("Hours left for task: " + this.getCurrentTask().getWorkingHours());
		System.out.println();
	}
}

