
public class Employee {

	private String name;
	private Task currentTask;
	private int hoursLeft;
	private AllWork allwork;

	Employee(String name) {
		if (name != null) {
			this.name = name;
		}
		this.setHoursLeft(8);
	}

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

	public void showReport() {
		System.out.println(this.getName() + " " + this.currentTask.getName() + " " + this.getHoursLeft() + " "
				+ this.currentTask.getWorkingHours());
	}

	public void work() {
		if (this.currentTask == null && this.allwork.getNextTask() != null) {
				this.currentTask = this.allwork.getNextTask();
		}
		if (currentTask.getWorkingHours() == 0) {
			if (allwork.getNextTask() != null) {
				this.currentTask = allwork.getNextTask();
			}
		}

		if (currentTask.getWorkingHours() >= this.getHoursLeft()) {
			currentTask.setWorkingHours(currentTask.getWorkingHours() - this.getHoursLeft());
			this.setHoursLeft(0);
		} else {
			this.setHoursLeft(this.getHoursLeft() - currentTask.getWorkingHours());
			currentTask.setWorkingHours(0);
		}
	}

	public void startWorkingDay() {
		this.hoursLeft = 8;
	}

	public AllWork getAllWork() {
		return allwork;
	}

	public void setAllWork(AllWork allwork) {
		if (allwork != null) {
			this.allwork = allwork;
		}
	}

}

