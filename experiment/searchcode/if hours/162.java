package itt_l14_encapsulation;

public class Employee {

	private static final int MAX_HOURS = 8;
	private static final int MIN_HOURS = 0;
	private static final int DEFAULT_HOURS = 0;
	static final String DEFAULT_NAME = "name";
	
	private String name;
	private double hoursLeft;
	private Task currentTask;

	// constructor
	public Employee(String name){
		setName(name);
	}
	
	void work(){
		if(getCurrentTask() != null){
			if(this.getHoursLeft() > this.getCurrentTask().getWorkingHours()){
				this.setHoursLeft(this.getHoursLeft() - this.getCurrentTask().getWorkingHours());
			} 
			else 
				if(this.getHoursLeft() < this.getCurrentTask().getWorkingHours()){
					this.setHoursLeft(0); 
					this.getCurrentTask().setWorkingHours(
							this.getCurrentTask().getWorkingHours() - this.getHoursLeft());
				} 
				else {
					this.setHoursLeft(0);
					this.getCurrentTask().setWorkingHours(0);
				}			
		}
		showReport();
	}
	
	
	void showReport(){
		System.out.println("Employee: " + this.getName() + 
				" \n Task: " + this.getCurrentTask().getName() + " " +
				"\n Working hours left: " + this.getHoursLeft() +
				"\n Hours of current task left to be done: " + this.getCurrentTask().getWorkingHours());
	}
	
	// get and set methods
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = (name != null) ? name : DEFAULT_NAME;
	}
	public double getHoursLeft() {
		return hoursLeft;
	}
	public void setHoursLeft(double hoursLeft) {
		this.hoursLeft = (hoursLeft >= MIN_HOURS && hoursLeft < MAX_HOURS) 
				? hoursLeft : DEFAULT_HOURS;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = (currentTask != null ) ? currentTask : new Task();
	}
}

