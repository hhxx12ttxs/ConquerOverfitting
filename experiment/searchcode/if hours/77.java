package HW_Task_Employee_Part_2;
public class Task {
	
	private static final int MIN_VALUE_WORKING_HOURS = 0;
	private static final int WRONG_VALUE_FOR_WORKING_HOURS = -9999;
	private String name;
	private int workingHours;
	
	Task(String name, int workingHours){
		setName(name);
		setWorkingHours(workingHours);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(name != null && !name.equals("")){
		this.name = name;
		} else {
			this.name = "Wrong name for taks";
		}
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if(workingHours >= MIN_VALUE_WORKING_HOURS){
		this.workingHours = workingHours;
		} else{
			this.workingHours = WRONG_VALUE_FOR_WORKING_HOURS;
		}
	}
}

