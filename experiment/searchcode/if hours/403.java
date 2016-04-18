package PackageWithEncapsulation;

public class Employee {
	final int MIN_TASK_TIME = 0;
	final int MIN_WORKER_TIME = 0;
	final private String name;
	Task currentTask;
	private int hoursLeft;
	
	//getter name
	public String getName(){
		return name;
	}
	//without setter, bacause worker name can not be changed
	
	//getter hoursLeft
	public int getHoursLeft(){
		return hoursLeft;
	}
	//setter hoursLeft
	public void setHoursLeft(int hoursLeft){
		if(hoursLeft>=MIN_WORKER_TIME){
			this.hoursLeft = hoursLeft;
		}
	}
	
	Employee(String name){
		if(name != null && ! name.equals("")){
			this.name = name;
		}
		else{
			this.name = "Invalid name";
		}
	}
	
	void work(){
		if(this.getHoursLeft()<this.currentTask.getWorkingHours()){
			this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.getHoursLeft());
			this.setHoursLeft(MIN_WORKER_TIME);
		}
		if(this.getHoursLeft()>=this.currentTask.getWorkingHours()){
			this.setHoursLeft(this.getHoursLeft() - this.currentTask.getWorkingHours());
			this.currentTask.setWorkingHours(MIN_TASK_TIME);
		}
	}
	void showReport(){
		System.out.println("Employee name:" + this.getName());
		System.out.println("Task for " + this.getName() + " is " + this.currentTask.getName());
		System.out.println("Working hours left to the worker:" + this.getHoursLeft());
		System.out.println("Working hours left for the task to be finished:" + this.currentTask.getWorkingHours());
		System.out.println();
	}
}

