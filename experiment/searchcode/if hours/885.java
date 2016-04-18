package workStatistic;

public class Employee {
	
	private String name;
	private Task currentTask;
	private int hoursLeft;
	
	Employee(String name) {
		this.setName(name);
	}
	
	public void setName(String name){
		if (name.length()< 2) {
			System.out.println("Name of the employee must be at least 2 characters long!");
		}
		else {
			this.name = name;
		}
	}
		
	public void setHoursLeft(int hours){
		if (hours < 0) {
			System.out.println("Hours must be non negative !");
		}
		else {
			this.hoursLeft = hours;
		}
	}
	
	public int getHoursLeft(){
		return this.hoursLeft;
	}
	
	public void setTask(Task newTask){
		this.currentTask = newTask;
	}
	
	void work(){
		if (this.currentTask.getWorkHours() <= this.hoursLeft) {
			this.setHoursLeft(this.hoursLeft - this.currentTask.getWorkHours());
			this.currentTask.setWorkHours(0);
		}
		else {
			this.currentTask.setWorkHours(this.currentTask.getWorkHours() - this.hoursLeft);
			this.setHoursLeft(0);
		}
	}
	
	void showReport(){
		System.out.println("Name of employee: "+this.name);
		System.out.println("Current task: "+ this.currentTask.getName());
		System.out.println("Hours left for employee: " + this.getHoursLeft());
		System.out.println("Task complete after: " + this.currentTask.getWorkHours());
	}
}

