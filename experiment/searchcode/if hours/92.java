public class Employee {
	private String employeeName;
	Task currentTask;
	private int hoursLeft;	//hours untill the end of the work day
	Employee(){						//constructor for working hours left
		this.hoursLeft = 8;
		
	}
	Employee(String name){			//constructor for employee name 
		this();
		setEmployeeName(name);
		
	}
	public void setHoursLeft(int hours){		//setter hoursleft
		if (hours>0&&hours<=8){
			this.hoursLeft = hours;
		}
		
	}
	public void setEmployeeName(String name) {		//setter employee name
		if(name.length()>0){
			this.employeeName = name;
		}
		
		
	}
	
	public int getHoursLeft(){
		return this.hoursLeft;
	}
	public String getEmployeeName(){
		return this.employeeName;
	}
	
	 void work(){
		int workHoursLeft = currentTask.getWokingHours();
		int employeeHours = getHoursLeft();
		
		if (workHoursLeft>=employeeHours){
			workHoursLeft-=employeeHours;
			employeeHours = 0;
		}
		else{
			employeeHours-=workHoursLeft;
			workHoursLeft = 0;
			
		}
		
		currentTask.setWorkingHours(workHoursLeft);
		setHoursLeft(employeeHours);
		
	}
	 void showReport(){
		 if(currentTask.getWokingHours()>0){
			 System.out.println("Name of worker: " + getEmployeeName());
			 System.out.println("Worker task: " + currentTask.getNameOfTask());
			 System.out.println("Worker hours left untill the end of the shift: " + getHoursLeft());
			 System.out.println("Hours untill the task is finished: " + currentTask.getWokingHours());
		 }
		 else{
			 System.out.println(getEmployeeName()+" has finnished his task");
			 System.out.println("Worker hours left untill the end of the shift: " + getHoursLeft());
		 }
	 }

}

