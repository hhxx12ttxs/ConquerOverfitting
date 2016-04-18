package employeeTask;

public class Employee {

	private String name;
	private Task currentTask;
	private double hoursLeft;

	Employee(String name) {
		setName(name);
		setHoursLeft(8);
	}

	public void setName(String name) {
		if (name != null) {
			for(int i = 0; i < name.length(); i++){
				if(name.charAt(i) < 'A' && name.charAt(i) > 'z'){
					return;
				}
			}
			this.name = name;
		}
	}
	
	public void setHoursLeft(double hoursLeft){
		if(hoursLeft >= 0 && hoursLeft <= 8){
			this.hoursLeft = hoursLeft;
		}
		else{ 
			this.hoursLeft = 8;
		}
	}

	public double getHoursLeft(){
		return hoursLeft;
	}
	
	public void setCurrentTask(Task currentTask){
		if(currentTask != null){
			this.currentTask = currentTask;
		} else { 
			System.out.println("There is no task");
		}
	}
	
	public Task getCurrentTask(){
		return this.currentTask;
	}
	
	void work(){
		if(this.currentTask != null){
			double workingHours = currentTask.getWorkingHours();
			if(workingHours > this.hoursLeft){
				workingHours -= this.hoursLeft;
				this.hoursLeft = 0;
			} else {
				this.hoursLeft -= workingHours;
				workingHours = 0;
			}
			currentTask.setWorkingHours(workingHours); 
		}
		
		showReport();
		
	}
	
	public void showReport(){
		System.out.println("Employee: " + this.name);
		System.out.println("Task: " + this.currentTask.getName());
		System.out.println("Employee's hours left: " + this.hoursLeft);
		System.out.println("Task's hours left: " + this.currentTask.getWorkingHours());
		System.out.println("---------------------------------");
	}
	

}

