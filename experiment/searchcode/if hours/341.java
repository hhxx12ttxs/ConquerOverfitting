package Task03;

public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name.equals("")) {
			System.out.println("No name!!!");
		} else {
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
		if (hoursLeft < 1) {
			System.out.println("hoursLeft can`t be less than 1");
		} else {
			this.hoursLeft = hoursLeft;
		}
	}

	public Employee(String name) {
		this.name = name;
	}

	void work() {
		if (hoursLeft > currentTask.getWorkingHours()) {
			hoursLeft = hoursLeft - currentTask.getWorkingHours();
			currentTask.setWorkingHours(0);
		} else if (hoursLeft < currentTask.getWorkingHours()) {
			currentTask.setWorkingHours(currentTask.getWorkingHours()
					- hoursLeft);
			hoursLeft = 0;
		} else {
			hoursLeft = 0;
			currentTask.setWorkingHours(0);
		}
	}

	void showReport() {
		System.out.print("Name of employee: " + this.name + "\nTask: "
				+ this.currentTask.getName()
				+ "\nRemaining working hours for employee: " + this.hoursLeft
				+ "\nHours till the task is done: "
				+ this.currentTask.getWorkingHours()+"\n\n");
	}

}

