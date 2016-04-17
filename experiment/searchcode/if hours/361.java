package lesson16_zad01_homework2;

public class Employee {

	private String name;
	private Task currentTask;
	private double hoursLeft;
	
	
	public Employee(String name){
		if(name != null){
			this.name = name;
		}
		else {
			this.name = "default name";
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		if(name != null){
			this.name = name;
		}
		else {
			this.name = "";
		}
	}
	
	public Task getCurrentTask(){
		return this.currentTask;
	}
	
	public void setCurrentTask(Task currentTask){
		if(currentTask != null){
			this.currentTask = currentTask;
		}
		else {
			this.currentTask = new Task("", 0.0);
		}
	}
	
	public double getHoursLeft(){
		return this.hoursLeft;
	}
	
	// Verifying data input
	public void setHoursLeft(double hoursLeft){
		if(hoursLeft > 0 && hoursLeft < 10){
			this.hoursLeft = hoursLeft;
		}
		else {
			this.hoursLeft = 0;
		}
	}
	
	// Work method
	public void work(){
		if(this.currentTask != null){
			if(this.currentTask.getWorkingHours() >= this.getHoursLeft()) {
				this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.getHoursLeft());
				this.setHoursLeft(0);
			}
			else if(this.getHoursLeft() > this.currentTask.getWorkingHours()) {
				this.setHoursLeft(this.getHoursLeft() - this.currentTask.getWorkingHours());
				this.currentTask.setWorkingHours(0);
			}
		}
	}
	
	// Print report to console
	public void showReport(){
		System.out.println("Employee name: " + this.getName() + 
				", \nTask name: " + this.currentTask.getName() +
				", \nData at the end of the working day: " +
				"\nEmployee's hours left: " + this.getHoursLeft() +
				", \nTask hours left: " + this.currentTask.getWorkingHours());
	}
	
}

