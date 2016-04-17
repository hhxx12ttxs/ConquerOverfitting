package WorkDemo;

public class Employee {

	final int workingHours = 8;
	private String name;
	private Task currentTask;
	private int hoursLeft;
	AllWork allWork;

	Employee(String name) {
		setName(name);
	}

	void startWorkingDay() {
		this.setHoursLeft(workingHours);
	}

	AllWork getAllWork() {
		return allWork;
	}

	void setAllWork(AllWork allWork) {
		this.allWork = allWork;
	}

	public void work() {
		if (currentTask == null) {
			setCurrentTask(allWork.getNextTask());
		}
		
		while(this.hoursLeft > 0 && currentTask != null) {
			if (currentTask == null) {
				break;
			}
			
			int hoursForWork = this.hoursLeft;
			if(this.hoursLeft > currentTask.getWorkingHours()) {
				hoursForWork = currentTask.getWorkingHours();
			}
			hoursLeft -= hoursForWork;
			currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursForWork);
			showReport();
			if (currentTask.getWorkingHours() == 0) {
				setCurrentTask(allWork.getNextTask());
			}
		}
	}


	public void showReport() {

		System.out.println("Worker name: " + this.getName());
		System.out.println("Work order name: " + this.currentTask.getTaskName());
		System.out.println("Working hours left: " + this.getHoursLeft());
		System.out.println("Order's hours left: " + this.currentTask.getWorkingHours());
		System.out.println();
	}

	// getters and setters
	public void setName(String name) {
		if (name.trim().isEmpty()) {
			System.out.println("You cannot input empty employee name!");
			return;
		}
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	public Task getCurrentTask() {
		return this.currentTask;
	}

	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft < 1) {
			return;
		}
		this.hoursLeft = hoursLeft;
	}

	public int getHoursLeft() {
		return this.hoursLeft;
	}

}

