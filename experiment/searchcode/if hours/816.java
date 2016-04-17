package Homework7_Encapsulation;

public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;
	
	Employee(String name){
		setName(name);
	}
	void work()
	{
		if(currentTask == null){
			System.out.println("No task to work on!");
			return;
		}
		if(currentTask.getWorkingHours() == 0){
			System.out.println("Task is already finished.");
			return;
		}
		if(currentTask.getWorkingHours() >= hoursLeft){
			currentTask.setWorkingHours(currentTask.getWorkingHours()-hoursLeft);
			hoursLeft = 0;
		}
		else{
			hoursLeft = hoursLeft - currentTask.getWorkingHours();
			currentTask.setWorkingHours(0);
		}
	}
	void showReport(){
		System.out.println("Employee name: " + name);
		System.out.println("Employee working hours: " + hoursLeft);
		currentTask.printInfo();
	}
	
	String getName(){
		return name;
	}
	Task getCurrentTask(){
		return currentTask;
	}
	int getHoursLeft(){
		return hoursLeft;
	}
	void setName(String name){
		if(name!=null && !name.isEmpty()){
			this.name = name;
		}
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
}

