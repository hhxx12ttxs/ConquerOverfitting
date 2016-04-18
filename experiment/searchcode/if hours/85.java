public class Employee {

	private String name;
	private Task currentTask;
	private int hoursLeft;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name;
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		if (currentTask != null) {
			this.currentTask = currentTask;
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		}
	}

	public Employee(String name) {
		if (name != null) {
			setName(name);
           setHoursLeft(3);
           
		}
	}

	public void work() {
		int workingHours;
		int temp = 0;
		System.out.println("The worker is working on his current task.");

		workingHours = currentTask.getWorkingHours();

		temp = workingHours;

		if (workingHours > this.hoursLeft) {
			temp -= this.hoursLeft;
			this.hoursLeft -= workingHours;
			workingHours=temp;
			}else if(workingHours<this.hoursLeft){
				workingHours-=hoursLeft;
				hoursLeft-=temp;
				
			}else{
				hoursLeft=0;
				currentTask.setWorkingHours(0);
			}
		
			if (hoursLeft < 0) {
				setHoursLeft(0);
			}
			if (workingHours<0) {
				currentTask.setWorkingHours(0);
			} else {
				currentTask.setWorkingHours(workingHours);
			}
		

		showReport();

	}

	public void showReport() {
		System.out.println(this.name);
		System.out.println(this.currentTask.getName());
		System.out.println(this.hoursLeft);
		System.out.println(this.currentTask.getWorkingHours());

	}

}

