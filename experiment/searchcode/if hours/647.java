
public class Employee {

	private String name;
	private Task currentTask;
	private double hoursLeft;
	private  AllWork allwork;
	
	Employee(String name,AllWork allwork) {
		setAllwork(allwork);
		this.name = name;
	}

	void startWorkingDay(){
		hoursLeft = 8;
		if(allwork.getCurrentUnassaignedTask()==10 && currentTask.getWorkingHours()==0){
			return;
		}
		System.out.println("Workday for "+name+" started");	
	}

	void work(){	

		if(allwork.getCurrentUnassaignedTask()==10 && currentTask.getWorkingHours()==0){
			hoursLeft=0;
			return;
		}
		if(this.getCurrentTask()==null || this.getCurrentTask().getWorkingHours()==0){
			if(allwork.getCurrentUnassaignedTask()==10){
				System.out.println("Workday for "+name+" is over");
				System.out.println(currentTask.getWorkingHours());
				hoursLeft=0;
				currentTask.setWorkingHours(0);
				return;
			}
			this.setCurrentTask(allwork.getNextTask());
        }		
		if(currentTask.getWorkingHours()==0){
			if(allwork.getCurrentUnassaignedTask()==10){
				System.out.println("Workday for "+name+" is over");
				System.out.println(currentTask.getWorkingHours());
				hoursLeft=0;
				currentTask.setWorkingHours(0);
				return;
			}
			currentTask = allwork.getNextTask();
		}
		
			System.out.println(name+" started "+currentTask.getName());
			if(hoursLeft >= currentTask.getWorkingHours()){
				hoursLeft-=currentTask.getWorkingHours();
				System.out.println(name+" finished "+currentTask.getName()+" (hours left for the day ) : "+hoursLeft);
				currentTask.setWorkingHours(0);
				if(hoursLeft>0){
					if(allwork.getCurrentUnassaignedTask()==10){
						System.out.println("Workday for "+name+" is over");
						hoursLeft=0;
						currentTask.setWorkingHours(0);
						return;
					}
				currentTask = allwork.getNextTask();
				System.out.println("Employee "+name+" started "+currentTask.getName());
				}
				else{
					System.out.println("Workday for "+name+" is over");
				}
			}
		    if(hoursLeft < currentTask.getWorkingHours()){
		    	double taskHoursLeft = currentTask.getWorkingHours() - hoursLeft;
		    	hoursLeft = 0;
		    	currentTask.setWorkingHours(taskHoursLeft);
		    	System.out.println("The work day for "+name+" is over");
		    	System.out.println(taskHoursLeft+" left for finishing the "+currentTask.getName());	
		    }
		}		
	

	public String getName(){
		return name;
	}
	public void setName(String name){
		if(name != null){
			this.name=name;
		}
	}
	public Task getCurrentTask(){
		return currentTask;
	}
	public void setCurrentTask(Task currentTask){
		if(currentTask != null){
			this.currentTask=currentTask;
		}
	}
	public double getHoursLeft(){
		return hoursLeft;
	}
	public void setHoursLeft(double hoursLeft){
		if(hoursLeft>=0){
			this.hoursLeft=hoursLeft;
		}
	}
	public AllWork getAllwork() {
		return allwork;
	}
	public void setAllwork(AllWork allwork) {
		if(allwork != null)
		this.allwork = allwork;
	}
	
}

