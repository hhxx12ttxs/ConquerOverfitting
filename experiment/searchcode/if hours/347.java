package lessons.lesson08.softacad;

public class Employee {
	private String name;
	private Task currentTask;
	private double hoursLeft;
	private AllWork allWork;
	
	public Employee(String name, AllWork allWork) {
		this.name = name;
		this.allWork = allWork;
//		this.hoursLeft = 8;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name != null && name.length() > 2) {
			this.name = name;
		}
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		this.currentTask = currentTask;
	}

	public double getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(double hoursLeft) {
		if(hoursLeft > 0) {
			this.hoursLeft = hoursLeft;
		}
	}
	
	public void work() {
		do {
			if(currentTask == null || currentTask.getWorkingHours() == 0) {
				currentTask = allWork.getNextTask();
			}
			
			if(currentTask != null) {
				if(currentTask.getWorkingHours() > hoursLeft) {
					currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursLeft);
					hoursLeft = 0;
				} else {
					hoursLeft -= currentTask.getWorkingHours();
					currentTask.setWorkingHours(0);
				}
			}
			
			showReport();
		} while(hoursLeft > 0 && currentTask != null);
	}
	
	public void startWorkingDay() {
		hoursLeft = 8;
	}
	
	private void showReport() {
		System.out.println("Employee name: " + name);
		System.out.println("Employee hours: " + hoursLeft);
//		if(currentTask != null) {
//			System.out.println("Task: " + currentTask.getName());
//			System.out.println("Task hours: " + currentTask.getWorkingHours());
//		}
		System.out.println("Task: " + (currentTask != null ? currentTask.getName() : " --no task--"));
		System.out.println("Task hours: " + (currentTask != null ? currentTask.getWorkingHours() : " --no task--"));
		System.out.println("-===-");
		System.out.println();
	}
	
}

