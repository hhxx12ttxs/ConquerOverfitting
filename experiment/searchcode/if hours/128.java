package lesson19_task_employee;

public class Employee {

	private static final int MAX_HOURS_LEFT = 10;
	private static final int MIN_HOURS_LEFT = 0;
	private static final int DEFAULT_HOURS_LEFT = 0;
	private static final int DEFAULT_WORK_HOURS = 8;
	private static final String DEFAULT_NAME = "name";
	
	protected static AllWork allwork;
	private String name;
	private Task currTask;
	private int hoursLeft;
	
	public Employee(String name){
		setName(name);
	}
	
	void startWorkDay(){
		this.setHoursLeft(DEFAULT_WORK_HOURS);
	}
	
	public void work(){
		
		System.out.println("work");
		int emplHours = 0;
		
		while(this.getHoursLeft() != 0){

			if (this.getCurrTask() == null ){
				this.currTask = Employee.allwork.getNextTask();
			}	
				
			this.showReport();
			// if curretn employee task is still null it means we didn't get the next task 
			// and reach the end of tasks array
			if(this.getCurrTask() == null){
				return;
			}
			
			if (this.getHoursLeft() > this.getCurrTask().getWorkHours()){
					
				emplHours = this.getHoursLeft() - this.getCurrTask().getWorkHours();
				
				this.setHoursLeft(emplHours);
				Employee.allwork.deleteTask();
				this.currTask = null;
				continue;

			}
			if (this.getHoursLeft() == this.getCurrTask().getWorkHours()){
				emplHours = 0;
				this.setHoursLeft(emplHours);
				Employee.allwork.deleteTask();
				this.currTask = null;
				continue;
			} 
			if (this.getHoursLeft() < this.getCurrTask().getWorkHours()) {
				int taskHours = this.getCurrTask().getWorkHours() - this.getHoursLeft();
					
				this.getCurrTask().setWorkHours(taskHours);
				this.setHoursLeft(0);
			}
		} 
	}
		
		
			
//		if(this.getCurrTask() == null){
//			this.setCurrTask(allwork.getNextTask());	
//			
//		}	
//		
//		int taskHours = this.getCurrTask().getWorkHours();
//		int emplHours = this.getHoursLeft();
//			
//		if(emplHours >= taskHours){
//			this.setHoursLeft(emplHours - taskHours); 
//			this.getCurrTask().setWorkHours(0);
//			allwork.deleteTask();
//			return;
//		}
//		else {
//			this.getCurrTask().setWorkHours(taskHours - emplHours);
//			this.setHoursLeft(0);	
//		}			
	
	
	public void showReport(){
		if(this.currTask != null){
			System.out.println(" " + this.getName() +  
								",\n  " + this.currTask.getName() + 
								"\n worker hours " + this.getHoursLeft() + 
								", task hours: " + this.currTask.getWorkHours());
		}
	}
	
	// get and set methods
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		this.name = (name != null) ? name : DEFAULT_NAME;
	}
	
	public Task getCurrTask(){ 
		return this.currTask;
	}
	
	public void setCurrTask(Task currTask){
		if(currTask != null){
			this.currTask = currTask; 
		}
	}
	
	public int getHoursLeft(){
		return this.hoursLeft;
	}
	
	public void setHoursLeft(int hoursLeft){
		this.hoursLeft = (hoursLeft >= MIN_HOURS_LEFT && hoursLeft <= MAX_HOURS_LEFT) ? 
				hoursLeft : DEFAULT_HOURS_LEFT;
	}

	public AllWork getAllwork() {
		return Employee.allwork;
	}
	
}

