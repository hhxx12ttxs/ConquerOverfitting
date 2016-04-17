
public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;

	Employee(String name) {
		this.setName(name); 
		}

	void work() {

		if ((getCurrentTask()).getWorkingHours() > this.getHoursLeft()) {
			(getCurrentTask()).setWorkingHours((getCurrentTask()).getWorkingHours() - this.getHoursLeft());
			this.setHoursLeft(0);
		} else {
			this.setHoursLeft(this.getHoursLeft() - (getCurrentTask()).getWorkingHours());
			(getCurrentTask()).setWorkingHours(0);
		}

		showReport();
	}

	void showReport() {
		System.out.println("Worker's name: " + this.getName());
		System.out.println("Current duty: " + this.currentTask.getName());
		System.out.println("Time left till the end of the working day: " + this.getHoursLeft() + " hours.");
		System.out.println("Hours left to complete the task: " + this.currentTask.getWorkingHours() + " hours.");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) {
		this.name = name;
		}else {
			this.name = "Default";
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
		if(hoursLeft >= 0){
		this.hoursLeft = hoursLeft;
		} else{
			this.hoursLeft = 0;
		}
	}
}

