package office;

public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;
	static  AllWork allwork;

	Employee(String name) {
		this.name = name;
	}

	 String getName() {
		return name;
	}

	 Task getCurrentTask() {
		return currentTask;
	}

	 void setCurrentTask(Task currentTask) {
		if (currentTask != null) {
			this.currentTask = currentTask;
		} else {
			System.out.println("Not a valid task");
		}
	}

	 int getHoursLeft() {
		return hoursLeft;
	}

	 void setHoursLeft(int hoursLeft) {
		if (hoursLeft > 0) {
			this.hoursLeft = hoursLeft;
		} else {
			System.out.println("Input valid hours");
		}
	}

	 void work() {
		startWorkingDay();
		
		 if (currentTask != null) {
			if (hoursLeft < currentTask.getWorkingHours()) {
				currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursLeft);
				hoursLeft = 0;
			} else {
				hoursLeft -= currentTask.getWorkingHours();
				currentTask.setWorkingHours(0);
			}
			report();
		} else {
			System.out.println("No Task");
		}
	}

	private void report() {
		System.out.println("Name of worker " + this.name + "\nName of worker's task " + currentTask.getName()
				+ "\nworking hours left for worker " + this.hoursLeft + "\nhours left for the task to be done "
				+ currentTask.getWorkingHours());
	}
	void startWorkingDay() {
		this.hoursLeft = 8;
	}
	AllWork getAllwork() {
		return allwork;
	}
	void setAllwork(AllWork allwork) {
		if(allwork != null) {
			Employee.allwork = allwork;
		} else {
			System.out.println("Not valid work");
		}
	}

}

