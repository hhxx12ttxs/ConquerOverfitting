

public class Employee {
	
	private String name;
	private Task currentTask;
	private int hoursLeft;
	private static AllWork allwork;
	
	public Employee(String name) {
		if(name.matches(".*\\d+.*" ) || name.trim().length() < 2){
			System.out.println("Invalid input. At least two letters and no digits are allowed for employee's name."
					+ "\nA person without a name is not eligible to work.");
		}else{
			this.name = name;
		}
	}
	
	public static AllWork getAllwork() {
		return allwork;
	}

	public static void setAllwork(AllWork allwork) {
		Employee.allwork = allwork;
	}

	public String getName() {
		return name;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		if(currentTask.getWorkingHours() < 0){
			System.out.println("This task was incorrectly inputted and the emplyee would not understand what to do.");
			System.out.println("Please redefine the parameters of the task.");
		}else{
			this.currentTask = currentTask;
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(int hoursLeft){
		if(hoursLeft < 0 || hoursLeft > 24){
			System.out.println("Invalid daily working hours input for the employee");
		}else{
			this.hoursLeft = hoursLeft;
		}
	}
	
	public void startWorkingDay(){
		this.hoursLeft = 8;
	}
	
	public void work(){
		if(this.name != null){
			
			if(this.currentTask != null && this.currentTask.getWorkingHours() != 0 ){
				if(this.currentTask.getWorkingHours() >= this.hoursLeft){
					this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.hoursLeft);
					this.hoursLeft = 0;
				}else{
					this.hoursLeft = this.hoursLeft - this.currentTask.getWorkingHours();
					this.currentTask.setWorkingHours(0);
				}
			}else{
				this.currentTask = allwork.getNextTask();
				if(this.currentTask != null){
					if(this.currentTask.getWorkingHours() >= this.hoursLeft){
						this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.hoursLeft);
						this.hoursLeft = 0;
					}else{
						this.hoursLeft = this.hoursLeft - this.currentTask.getWorkingHours();
						this.currentTask.setWorkingHours(0);
					}
				}
			}
		}
		
	}
	
	void showReport(){
		if(this.name == null){
			System.out.println("To show a report pleae assaign a name to your employee first");
		}else if(this.currentTask == null || this.currentTask.getWorkingHours() < 0){
			System.out.println("To show a report pleae assaign a task to your employee first");
		}else{
			System.out.println("Task Report");
			System.out.println("The employee name: " + this.name);
			System.out.println("The employee task: " + this.currentTask.getName());
			System.out.println("The hours left on the task: " + this.currentTask.getWorkingHours());
			System.out.println("The employee's working hours left for the day: " + this.hoursLeft);
		}
	}
	
}

