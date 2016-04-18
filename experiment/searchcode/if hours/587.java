package oop.office;

public class Employee {

	private String name;

	private Task currentTask;

	private int hoursLeft;

	private AllWork allWork;

	public Employee(String name, AllWork allWork) {
		this.name = name;
		this.hoursLeft = 8;
		this.allWork = allWork;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && name != "") {
			this.name = name;
		} else {
			System.out.println(name + " is not valid for Employee name !");
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		if (currentTask != null) {
			System.out.println("Assigning task \"" + currentTask.getName()
					+ " \" to " + name);
		}
		this.currentTask = currentTask;
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft > 0) {
			this.hoursLeft = hoursLeft;
		} else {
			System.out.println(hoursLeft
					+ " is not valid for Employee working time !");
		}
	}

	public void work() {
		while (currentTask != null && hoursLeft > 0) {
			if (hoursLeft >= currentTask.getWorkingHours()) {
				int workedHoursOnTask = currentTask.getWorkingHours();
				hoursLeft = hoursLeft - workedHoursOnTask;
				currentTask.setWorkingHours(0);
				showReport(workedHoursOnTask, true);
				setCurrentTask(allWork.getNextTask());
				work();
			} else {
				currentTask.setWorkingHours(currentTask.getWorkingHours()
						- hoursLeft);
				int workedHoursOnTask = hoursLeft;
				hoursLeft = 0;
				showReport(workedHoursOnTask, false);
			}

		}

	}

	public void showReport(int workedHours, boolean isDone) {
		System.out.println(name + " is working on task \""
				+ currentTask.getName() + "\" for " + workedHours + " hours" + (isDone ? " - DONE" : ""));
	}

	public void startWorkingDay() {
		hoursLeft = 8;
	}

}

