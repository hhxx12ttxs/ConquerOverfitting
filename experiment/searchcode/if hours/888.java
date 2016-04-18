package WorkDemo;

public class Employee {
	
	private String name;
	private Task currentTask;
	private int hoursLeft;
	
	Employee(String name) {
		setName(name);
	}
	
	public void setName(String name) {
		if (name.trim().isEmpty()) {
			System.out.println("You cannot input empty employee name!");
			return;
		}
		this.name = name; 
	}
	
	public String getName() {
		return name;
	}
	
	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}
	
	public Task getCurrentTask() {
		return currentTask;
	}
	
	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft < 1) {
			return;
		}
		this.hoursLeft = hoursLeft;
	}
	
	public int getHoursLeft() {
		return hoursLeft;
	}
	
	void work() {
		
		if (this.currentTask != null) {
			if(this.currentTask.getWorkingHours() <= this.getHoursLeft()) {
				this.hoursLeft -= this.currentTask.getWorkingHours();
				this.currentTask.setWorkingHours(0);
			} else {
				this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.getHoursLeft());
				this.hoursLeft = 0;
			}
			
			showReport();
		}
	}
	
	private void showReport() {
		System.out.println("Worker name: " + this.getName());
		System.out.println("Work order name: " + this.currentTask.getTaskName());
		System.out.println("Working hours left: " + this.getHoursLeft());
		System.out.println("Order's hours left: " + this.currentTask.getWorkingHours());
		System.out.println();
	}
	
}	

