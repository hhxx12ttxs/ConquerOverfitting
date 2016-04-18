package TaskAndEmployee;

public class Employee {
	
	private String name;
	private Task currentTask;
	private int hoursLeft;
	
	
	
	public Employee(String name){
		
		setName(name);
	}
	
	
	private boolean isLetter(String s){
		
		boolean flag = true;
		int i = 0;
		while(i < s.length()){
		     if(s.charAt(i) >='a' && s.charAt(i) <= 'z' || s.charAt(i) >='A' && s.charAt(i) <= 'Z' || s.charAt(i) == ' ')
		    	 i++;
		     else{
		    	 flag = false;
		    	 break;
		     }
		     
		}
		return flag;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		

		if(name != " " && isLetter(name)){
			this.name = name;
		}
		
	}


	public Task getCurrentTask() {
		return currentTask;
	}


	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}


	public int getHoursLeft() {
		return hoursLeft;
	}


	public void setHoursLeft(int hoursLeft) {
		
		if(hoursLeft >= 0)
		this.hoursLeft = hoursLeft;
	}


	void work(){
		
		Task newTask = new Task(" ", 0);
		newTask = getCurrentTask();
		int workingHours = newTask.getWorkingHours();
		
		if(this.getCurrentTask() != null){
			
			if(this.hoursLeft > workingHours){
				
				this.hoursLeft -= workingHours;
				newTask.setWorkingHours(0);
				
			}
			
			else
				if(this.hoursLeft < workingHours){
					
					workingHours = workingHours - this.getHoursLeft();
					newTask.setWorkingHours(workingHours);
					this.setHoursLeft(0);
				}
			
				else{
					
					this.setHoursLeft(0);
					newTask.setWorkingHours(0);
					
					
				}
			
			this.setCurrentTask(newTask);
			
		}
		else
			return;
	}
	
	
	public void showReport(){
		
		System.out.println("Name: " + this.getName());

		Task newTask = new Task(" ", 0);
		newTask = getCurrentTask();
		System.out.println("Task: " + newTask.getName());
		
		System.out.println("Hours left for today: " + this.getHoursLeft());
		System.out.println("Hours needed to finish the task: " + newTask.getWorkingHours());
	}
}

