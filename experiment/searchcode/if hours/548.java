package homework9;

class Task {
	
	
	private static final String DEFAULT_TASK_NAME = "Task";
	static final int MIN_WORKING_HOURS = 0;
	
	private String name;
	private int workingHours;
	private Employee employee;
	
	Task(String name, int workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}
	
	
	public String getName() {
		return name;
	}
	void setName(String name) {
		if (name != null && !name.equals("")) {
			this.name = name;
		}
		else {
			this.name = DEFAULT_TASK_NAME;
		}
		
	}
	public int getWorkingHours() {
		return workingHours;
	}
	void setWorkingHours(int workingHours) {
		if (workingHours > MIN_WORKING_HOURS) {
			this.workingHours = workingHours;
		}
		else {
			this.workingHours = MIN_WORKING_HOURS;
		}
		
	}


	public Employee getEmployee() {
		return employee;
	}


	void setEmployee(Employee employee) {
		if (employee != null) {
			this.employee = employee;
		}
		else {
			System.out.println("No such employee for " + this.name);
		}
		
	}
	

}

