package com.pragmatic.lesson5.inheritance.homework.office;

public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;
	private static AllWork allwork;
	
	public Employee(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name != null && !name.equals("")) {
			this.name = name;
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		if( currentTask != null && currentTask != this.currentTask) {
			this.currentTask = currentTask;
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(int hoursLeft) {
		if(hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		}
	}
	
	public void work(){
		while(hoursLeft > 0) {
			if(currentTask == null || currentTask.getWorkingHours() == 0) {
				currentTask = allwork.getNextTask();
				if(currentTask == null)
					return;
				System.out.println("Assigning task " + currentTask.getName() + " to " + name);
			}
			
			int taskWorkingHours = currentTask.getWorkingHours();
			
			if ( taskWorkingHours != 0){
				int possibleHours = (hoursLeft >= taskWorkingHours) ? taskWorkingHours : hoursLeft;
				hoursLeft -= possibleHours;
				currentTask.setWorkingHours(taskWorkingHours - possibleHours);
				System.out.println(name + " is working on task " + currentTask.getName() + " for " + possibleHours + " hours.");
			}
		}
	}
	
	public void showReport() {
		System.out.println("Employee name: " + name);
		System.out.println("Employee's current task name: " + currentTask.getName());
		System.out.println("Hours left to the end of day: " + hoursLeft);
		System.out.println("Current task needs " + currentTask.getWorkingHours() + " to be completed");
	}

	public static AllWork getAllwork() {
		return allwork;
	}

	public static void setAllwork(AllWork work) {
		allwork = work;
	}
	
	public void startWorkingDay() {
		hoursLeft = 8;
	}
}

