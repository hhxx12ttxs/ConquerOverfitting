package Encapsulation_Homework;
public class Task {
	
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
		if(name != null && name != ""){
		this.name = name;
		} else {
			this.name = "Wrong name for taks";
		}
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if(workingHours >= 0){
		this.workingHours = workingHours;
		} else{
			this.workingHours = -9999;   // umishleno e taka za da napravi vpechatlenie che e greshno
		}
	}
}

