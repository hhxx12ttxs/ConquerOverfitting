package hw07;

public class Employee {

	// attributes

	private String name;
	private Task currentTask;
	private int hoursLeft; // Employee's working hours left till the end of the
							// day

	// constructor

	Employee(String name) {
		setName(name);
//		if (name != null) {
//			this.name = name;
//		}

	}

	// methods

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
		if (currentTask != null && !currentTask.equals("")) {
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
		if(this.currentTask!=null){
		if (this.hoursLeft >= this.currentTask.getWorkingHours()) {
			int empHours = this.hoursLeft - this.currentTask.getWorkingHours();
			this.currentTask.setWorkingHours(0);
			this.setHoursLeft(empHours);
		} else {
			int taskLeftHours = this.currentTask.getWorkingHours()
					- this.hoursLeft;
			this.currentTask.setWorkingHours(taskLeftHours);
			this.setHoursLeft(0);
		}
		}
		else{
			System.out.println("No task to work on");
		}
		this.showReport();

	}

	public void showReport() {
		System.out.println("Name of Employee : " + this.name);
		
		System.out.println("Employee's hours left : " + this.hoursLeft);
		if(this.currentTask!=null){
		System.out.println("Task name : " + this.currentTask.getName());
		System.out.println("Task's hours left : "
				+ this.currentTask.getWorkingHours());
		System.out.println();
		}
	}
}

