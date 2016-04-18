package hw07;

public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;
	
	Employee(String name) {
		this.name = name;
	}

	Employee(String name, Task currentTask, int hoursLeft) {
		this.name = name;
		this.currentTask = currentTask;
		this.hoursLeft = hoursLeft;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name.equals("")) {
			System.out.println("error: no name");
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
		if(hoursLeft > 0) {
			this.hoursLeft = hoursLeft;
		} else {
			System.out.println("error: hours must be signed");
		}
	}
	
	public void work () {
		if (currentTask.getWorkingHours() > hoursLeft) {
			currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursLeft);
		} else {
			currentTask.setWorkingHours(0);
		}
	}
	
	public void showReport () {
		System.out.println("------------------------------------------------------");
		System.out.println("Name: " + name);
		System.out.println("Current task name: " + currentTask.getName());
		//System.out.println("Task completion: " + (currentTask.getWorkingHours() + hoursLeft));
		System.out.println("Hours remaining: " + hoursLeft);
		System.out.println("Hours remaing for the current task to be completed: " + currentTask.getWorkingHours());
	}
}
