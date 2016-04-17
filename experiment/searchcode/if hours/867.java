package office;

public class Employee {
	private static final int WORKDAY_LENGHT = 8;
	private String name;
	private Task currentTask;
	private int hoursLeft;
	private AllWork allWork;

	private void showReport() {
		System.out.println(this.name + " after working on " + this.currentTask.getName() + " has left " + this.hoursLeft
				+ " hours and has to work " + this.currentTask.getWorkingHours() + " more hours to complete it.");
	}

	public Employee() {
		name = "Unnamed";
		hoursLeft = WORKDAY_LENGHT;
		currentTask = new Task("Chilling", 0);
	}

	public void startWorkingDay() {
		this.hoursLeft = WORKDAY_LENGHT;
	}

	public Employee(String name) {
		this();
		if (name != null && !name.equals("")) {
			this.name = name;
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) {
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

	public void work() {
		if (this.hoursLeft <= 0) {
			return;
		}
		if (allWork != null && currentTask.getWorkingHours() <= 0) {
			currentTask = allWork.getNextTask();
		}

		if (this.hoursLeft > this.currentTask.getWorkingHours()) {
			this.hoursLeft -= this.currentTask.getWorkingHours();
			this.currentTask.setWorkingHours(0);
		} else {
			this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.hoursLeft);
			this.hoursLeft = 0;
		}
		showReport();
		if (this.hoursLeft > 0) {
			this.setCurrentTask(this.allWork.getNextTask());
			if (this.currentTask.getWorkingHours() > 0) {
				System.out.print("Another task for ");
				this.work();
			}
		}
	}

	public AllWork getAllWork() {
		return allWork;
	}

	public void setAllWork(AllWork allWork) {
		if (allWork != null) {
			this.allWork = allWork;
		}
	}

}

