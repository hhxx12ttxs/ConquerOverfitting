
public class Employee {
	private final int END_OF_WORKING_DAY = 0;
	private  final int BEGINNING_OF_WORKING_DAY = 8;
	
	private String name;
	Task currentTask;
	double hoursLeft;
	
	public void setName (String newName){
		if (newName!=null && !newName.equals("")){
			this.name=newName;
		}
		
	}
	
	public void setHoursLeft (double newHoursLeft){
		if (newHoursLeft>=END_OF_WORKING_DAY && newHoursLeft<=BEGINNING_OF_WORKING_DAY){
			this.hoursLeft=newHoursLeft;
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	Employee (String newName){
		setName(newName);
	}
	
	void work (){
		if(this.currentTask.getName()!=null && this.currentTask.getName()!=""){
			if (this.hoursLeft>currentTask.workingHours){
				this.hoursLeft-=currentTask.workingHours;
				currentTask.workingHours=END_OF_WORKING_DAY;
			}
			if (this.hoursLeft<currentTask.workingHours){
				currentTask.workingHours-=this.hoursLeft;
				this.hoursLeft=END_OF_WORKING_DAY;
			}
			if (this.hoursLeft==currentTask.workingHours){
				this.hoursLeft=END_OF_WORKING_DAY;
				currentTask.workingHours=END_OF_WORKING_DAY;
			}
		}
		else {
			System.out.println(this.getName()+" has no task.");
		}
	}
		
		void showReport(){
			System.out.println("Name: "+this.getName());
			System.out.println("Task: "+currentTask.getName());
			System.out.println("Employee's left hours: "+this.hoursLeft);
			System.out.println("Hours left fot the task to be done: "+currentTask.workingHours);
		}
	}
	
	
	

