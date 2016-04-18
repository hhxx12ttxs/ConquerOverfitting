package hw07;

public class Employee {
	private String name;
	Task currentTask;
	private int hoursLeft;

	Employee(String name, Task currentTask, int hoursLeft) {
		this.name = name;
		this.currentTask = currentTask;
		this.hoursLeft = hoursLeft;
	}

	Employee(String name) {
		this.name = name;
	}

	public void setName(String name) {
		if (!name.equals(" ")) {
			this.name = name;
			System.out.println("The name :" + name);
		}
	}

	public void setHourLeft(int hoursLeft) {
		if (hoursLeft > 0) {
			this.hoursLeft = hoursLeft;
		}
	}

	public String getName() {
		System.out.println("The name :" + name);
		return name;

	}

	public int getHoursLeft() { 
		System.out.println("The hours :" + hoursLeft);
		return hoursLeft;
	}

	int n;

	int work() {
		
		if (currentTask.getWorkingHours() >= hoursLeft) {
			n = currentTask.getWorkingHours() - hoursLeft;
			System.out.println("The time left for the worker:" + n);
			System.out.println("The job is done.");
			return n;
		} else {
			System.out.println("The time left for  the worker :" + 0);
			System.out.println("The job needs more hours to be done:"+(hoursLeft-currentTask.getWorkingHours()));
			return 0;
		}
	}

	void showReport() {
		this.work();

		System.out.println("The worker's name is  " + name);
		System.out.println("The task is " + currentTask.getName());
		System.out.println("The worker has " + hoursLeft + " hours left.");
		System.out.println("The left hours for the task are " + n);
	}
}

