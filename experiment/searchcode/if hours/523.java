
public class Employee {

	private String name;
	private Task task;
	private int hoursLeft;

	public Employee(String name) {
		if (name != null) {
			this.name = name;
		} else {
			this.name = "Unnamed employee";
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && name != "") {
			this.name = name;
		} else {
			this.name = "Unnamed employee";
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		} else {
			this.hoursLeft = 0;
		}
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		if (task != null) {
			this.task = task;
		} else {
			task = new Task();
		}
	}

	public void work() {
		if (this.task != null) {
			if (this.hoursLeft < this.task.getWorkingHours()) {
				this.task.setWorkingHours(this.task.getWorkingHours() - this.hoursLeft);
				setHoursLeft(0);
			} else {
				setHoursLeft(this.hoursLeft - this.task.getWorkingHours());
				this.task.setWorkingHours(0);
			}
			if (this.hoursLeft == 0) {
				System.out.println(this.name + " has done his work for today");
			}
			if (this.task.getWorkingHours() == 0) {
				System.out.println(this.task.getName() + " have been done");
			}
		} else {
			System.out.println("The employee " + this.name + " has no task");

		}
	}
	
	public void printInfo() {
		System.out.println("Employee name: " + this.name);
		System.out.println("Employee hours left: " + this.hoursLeft);
		task.printInfo();
	}
	
	public void showReport() {
		this.printInfo();
	}

}

