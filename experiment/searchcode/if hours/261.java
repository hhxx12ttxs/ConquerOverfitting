package work;

public class Employee {
	private String name;
	private Task currentTask;
	private double hoursLeft;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name;
		} else {
			System.out.println("Please enter a valid name");
			setName(name);
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		if (currentTask != null) {
			this.currentTask = currentTask;
		} else {
			System.out.println("Please enter a valid task");
			setCurrentTask(currentTask);
		}
	}

	public double getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(double hoursLeft) {
		if (hoursLeft > 0) {
			this.hoursLeft = hoursLeft;
		} else {
			System.out.println("Please enter a valid amount of hours left");
			setHoursLeft(hoursLeft);
		}
	}

	public Employee(String name) {
		setName(name);
	}
	public void work(){
		if(this.hoursLeft>currentTask.getWorkingHours()){
			int temp=currentTask.getWorkingHours();
			currentTask.setWorkingHours(0);
			this.hoursLeft-=temp;
			
		} else {
			if(this.hoursLeft==currentTask.getWorkingHours()){
				int temp=currentTask.getWorkingHours();				
				currentTask.setWorkingHours(temp-(int)this.hoursLeft);
				this.hoursLeft=0;
			} else{
				currentTask.setWorkingHours(0);
				this.hoursLeft=0;
			}
		}
		showReport();
	}
	public void showReport(){
		System.out.println("The employee "+this.getName()+" worked on task "+this.getCurrentTask()+" has "+this.getHoursLeft()+" working hours left and "+this.currentTask.getWorkingHours()+" hours to cocmplete his task"); 
	}
}

