package homework9;

class Employee {

	private static final int MAX_WORKING_HOURS_LEFT = 8;
	static final int MIN_WORKING_HOURS_LEFT = 0;
	private final String DEFAULT_EMPLOYEE_NAME = "Employee name";
	
	private final String name;
	private Task currentTask;
	private int hoursLeft;
	private AllWork allwork;
	
	Employee(String name) {
		if (name != null && !name.equals("")) {
			this.name = name;
		}
		else {
			this.name = DEFAULT_EMPLOYEE_NAME;
		}
	}
	
	void work() {
		if ((this.getCurrentTask() == null || this.getCurrentTask().getWorkingHours() == Task.MIN_WORKING_HOURS) 
				&& !this.allwork.isAllWorkDone()) {
			System.out.println(this.name + " is starting to do " + allwork.getNextTask().getName());
			this.setCurrentTask(allwork.getNextTask());
		}
		if (this.getCurrentTask().getWorkingHours() == Task.MIN_WORKING_HOURS) {
			System.out.println("There is no more work for " + this.name);
			return;
		}
		
		if (this.hoursLeft >= this.currentTask.getWorkingHours()) {
			System.out.println(this.name + " finished doing " + this.getCurrentTask().getName());
			this.hoursLeft -= this.currentTask.getWorkingHours();			
			this.currentTask.setWorkingHours(Task.MIN_WORKING_HOURS);
			this.work();
		}
		else {	
			System.out.println("Working day is finished for " + this.name);
			this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.hoursLeft);
			this.hoursLeft = MIN_WORKING_HOURS_LEFT;
		}	
	}
	
	void startWorkingDay() {
		System.out.println(this.name + " is starting work. ");
		this.setHoursLeft(MAX_WORKING_HOURS_LEFT);
		if (this.getCurrentTask() != null 
				&& this.getCurrentTask().getWorkingHours() > Task.MIN_WORKING_HOURS) {
			System.out.println(this.name + " is still working on " 
				+ this.getCurrentTask().getName());
		}
	}
	
	void showReport() {
		System.out.println(this.name + " " + this.currentTask.getName() + " " 
				+ this.hoursLeft + " " + this.currentTask.getWorkingHours());
	}
	
	
	public String getName() {
		return name;
	}
	
	public Task getCurrentTask() {
		return currentTask;
	}
	void setCurrentTask(Task currentTask) {
		if (currentTask != null && currentTask.getWorkingHours() > Task.MIN_WORKING_HOURS 
				&& currentTask.getEmployee() == null) {
			this.currentTask = currentTask;	
			this.currentTask.setEmployee(this);
			this.allwork.setCurrentUnassignedTask(this.allwork.getCurrentUnassignedTask() + 1);
		}
		else {
			System.out.println("This task is unavailable or it is finished");
		}
		
	}
	public int getHoursLeft() {
		return hoursLeft;
	}
	void setHoursLeft(int hoursLeft) {
		if (hoursLeft > MIN_WORKING_HOURS_LEFT && hoursLeft <= MAX_WORKING_HOURS_LEFT) {
			this.hoursLeft = hoursLeft;
		}
		if (hoursLeft <= MIN_WORKING_HOURS_LEFT) {
			this.hoursLeft = MIN_WORKING_HOURS_LEFT;
		}
		if (hoursLeft > MAX_WORKING_HOURS_LEFT) {
			this.hoursLeft = MAX_WORKING_HOURS_LEFT;
		}
		
	}

	public AllWork getAllwork() {
		return allwork;
	}

	void setAllwork(AllWork allwork) {
		if (allwork != null) {
			this.allwork = allwork;
		}		
	}
	
	
}

