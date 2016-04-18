package employeePart2Abstr;

public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;
	private static AllWork allWork;

	Employee(String name) {
		setName(name);
		setHoursLeft(8);
	}
	
	public static void setAllWork(AllWork allWork){
		if(allWork != null){
			Employee.allWork = allWork;
		}
	}

	void setName(String name) {
		if (name != null) {
			this.name = name;
		}
	}

	String getName() {
		return this.name;
	}

	void setHoursLeft(int hoursLeft) {
		if (hoursLeft >= 0 && hoursLeft <= 8) {
			this.hoursLeft = hoursLeft;
		} else {
			this.hoursLeft = 8;
		}
	}

	int getHoursLeft() {
		return hoursLeft;
	}

	Task getCurrentTask() {
		return this.currentTask;
	}

	void startWorkingDay() {
		this.hoursLeft = 8;
	}

	void work() {

		startWorkingDay();
		do {
			if(this.currentTask == null || this.currentTask.getWorkingHours() == 0){
				this.currentTask = allWork.getNextTask();
				
			}
			if(this.currentTask == null ){
				return;
			} else {
				if (this.hoursLeft >= this.currentTask.getWorkingHours()) {
				     this.hoursLeft -= this.currentTask.getWorkingHours();
				     this.currentTask.setWorkingHours(0);
				    } else {
				     this.currentTask.setWorkingHours(this.currentTask.getWorkingHours() - this.hoursLeft);
				     this.hoursLeft = 0;
				    }
				showReport();
			}
		} while(this.hoursLeft != 0 && this.currentTask.getWorkingHours() == 0);
	}
	
	void showReport() {
		System.out.println("Employee: " + this.name);
		System.out.println("Task: " + this.currentTask.getName());
		System.out.println("Employee's hours left: " + this.hoursLeft);
		System.out.println("Task's hours left: " + this.currentTask.getWorkingHours());
		System.out.println("---------------------------------");
	}
}

