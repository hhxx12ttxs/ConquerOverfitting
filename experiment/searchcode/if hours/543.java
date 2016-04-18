package Homework9;

public class Employee {
	static final int MAX_WORKING_HOURS=8;
	private String name;
	private Task currentTask;
	private int hoursLeft;
	private AllWork allwork;
	
	Employee(String name){
		if(name!=null && !name.isEmpty()){
			this.name = name;
		}
	}
	
	public void startWorkingDay(){
		hoursLeft = MAX_WORKING_HOURS;
	}
	
	public void work()
	{
		while(hoursLeft>0 && !allwork.isAllWorkDone()){
			if(currentTask == null ){
				currentTask = allwork.getNextTask();
				System.out.println(name + " starts working on " + currentTask.getName());
			}
			if(currentTask.getWorkingHours() == 0 ){
				currentTask = allwork.getNextTask();
				System.out.println(name + " starts working on " + currentTask.getName());
			}
			if(currentTask.getWorkingHours() > hoursLeft){
				currentTask.setWorkingHours(currentTask.getWorkingHours()-hoursLeft);
				hoursLeft = 0;
				System.out.println(name + " finishes the working day with " +
						currentTask.getWorkingHours() + " hours to finish " + currentTask.getName());
			}
			if(currentTask.getWorkingHours() <= hoursLeft){
				hoursLeft -= currentTask.getWorkingHours();
				currentTask.setWorkingHours(0);
				System.out.println(name + " finishes " + currentTask.getName() + 
						" with " + hoursLeft + " hours to work");
			}	
			if(!(allwork.getCurrentUnassignedTask()<=9)){
				break;
			}

		}
		System.out.println("");
	}

	public String toString() {
		return "Employee name: " + name + "\n"
				+ "Employee working hours: " + hoursLeft;
	};
	String getName(){
		return name;
	}
	
	Task getCurrentTask(){
		return currentTask;
	}
	
	int getHoursLeft(){
		return hoursLeft;
	}
	
	void setCurrentTask(Task curTask){
		if(curTask!=null){
			currentTask = curTask;
		}
	}
	
	void setHoursLeft(int hoursLeft){
		if(hoursLeft>=0){
			this.hoursLeft = hoursLeft;
		}
	}
	
	public AllWork getAllWork(){
		return allwork;
	}
	
	void setAllWork(AllWork allwork){
		if(allwork!=null){
			this.allwork = allwork;
		}
	}
}

