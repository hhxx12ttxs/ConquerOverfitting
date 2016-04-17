package the_work;

public class Task {
	
	
	private String name;
	private int workingHours;
	
	public String getName() {
		return name;
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		
		if (workingHours < 1 || workingHours > 8){
			System.out.println("Please enter valid hours");
		}else{
			this.workingHours = workingHours;
		}
	}

	void task (String nameOfTask, int hoursForTask){
		if (nameOfTask == null){
			System.out.println("Enter a valid task");
		}else{
			this.name = nameOfTask;
		}
		
		if (hoursForTask < 1 || hoursForTask > 8){
			System.out.println("Please enter valid hours");
		}else{
			this.workingHours = hoursForTask;
		}
	}
	
	

}

