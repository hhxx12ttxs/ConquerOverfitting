package lesson08;

public class Employee {
	public static int ONE_DAY_WORKING_HOURS = 8;

	private String name;
	private Task currentTask;
	private int hoursLeft;
	private static AllWork allWork;

	public Employee(String name) {
		setName(name);
		hoursLeft = ONE_DAY_WORKING_HOURS;
	}

	public void setName(String name) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = "";
		}
	}

	public String getName() {
		return name;
	}

	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setCurrentTask(Task currentTask) {
		if (currentTask != null && currentTask.getWorkingHours() > 0) {
			this.currentTask = currentTask;
		} else {
			System.out.println("Not valid task!!!");
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public static void setAllWork(AllWork work) {
		allWork = work;
	}

	public static AllWork getAllWork() {
		return allWork;
	}

	public void work() {
		while (this.hoursLeft > 0) {
			if (currentTask == null || currentTask.getWorkingHours() == 0) {
				currentTask = allWork.getNextTask();
				if (currentTask == null) {
					System.out.println("No free task for " + name);
					return;
				}
			}

			int hoursForWork = this.hoursLeft;
			if (this.hoursLeft > currentTask.getWorkingHours()) {
				hoursForWork = currentTask.getWorkingHours();
			}
			hoursLeft -= hoursForWork;
			currentTask.setWorkingHours(currentTask.getWorkingHours()
					- hoursForWork);
			showReport();
		}

	}

	public void showReport() {
		System.out.println("--------------------------");
		System.out.println("Employee name: " + name);
		System.out.println("Task: " + currentTask.getName());
		System.out.println("Employee hours left: " + hoursLeft);
		System.out.println("Task hours left: " + currentTask.getWorkingHours());
		System.out.println("--------------------------");
	}

	public void startWorkingDay() {
		hoursLeft = ONE_DAY_WORKING_HOURS;
		System.out.println(name + " start working day");
	}

}

