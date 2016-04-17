package PackageWithEncapsulation;

public class Task {
	final int MIN_TASK_TIME = 0;
	private String name;
	private int workingHours;
	
	//getter name
	public String getName(){
		return name;
	}
	
	//setter name
	public void setName(String name){
		if(name != null){
			this.name = name;
		}
	}
	
	//getter workingHours
	public int getWorkingHours(){
		return workingHours;
	}
	//setter workingHours
	public void setWorkingHours(int workingHours){
		if(workingHours>=MIN_TASK_TIME){
			this.workingHours = workingHours;
		}
	}
	
	Task(String name, int workingHours){
		setName(name);
		setWorkingHours(workingHours);
	}
}

