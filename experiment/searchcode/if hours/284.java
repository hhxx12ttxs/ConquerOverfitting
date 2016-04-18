package task1;

public class Employee {
	private String name;
	private Task currentTask;
	private double hoursLeft;

	protected String getName() {
		return name;
	}

	protected void setName(String name) {
		if (!((name.equals("")) && (name.equals(null)))) {
			this.name = name;
		}
	}

	protected Task getCurrentTask() {
		return currentTask;
	}

	private void setCurrentTask(Task currentTask) {
		if (!((currentTask.equals("")) && (currentTask.equals(null)))) {
			this.currentTask = currentTask;
		}
	}

	protected double getHoursLeft() {
		return hoursLeft;
	}

	protected void setHoursLeft(double hoursLeft) {
		if (hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		}
	}

	void work(Task nextTask) {
		setCurrentTask(nextTask);
		double taskWorkingHours = nextTask.getWorkingHours();
		double employeeHoursLeft = this.getHoursLeft();
		if (employeeHoursLeft > 0) {
			if (employeeHoursLeft >= taskWorkingHours) {
				this.setHoursLeft(employeeHoursLeft - taskWorkingHours);
				nextTask.setWorkingHours(0);
			} else {
				this.setHoursLeft(0);
				nextTask.setWorkingHours(taskWorkingHours - employeeHoursLeft);
			}
		}else{
			System.out.println("It's time to stop working for today and go home!");
		}
	}
	void showReport( Task thisTask){
		work(thisTask);
		System.out.println("Employee: "+this.getName());
		System.out.println("Current task: "+thisTask.getName());
		System.out.println("Employee's working hours left : "+this.getHoursLeft());
		System.out.println("Working hours left to be completed the task: "+thisTask.getWorkingHours());
	}
}

