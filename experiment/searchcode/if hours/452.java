package homework_abstraction;

public class Task {
	
	private String taskName;
	private int workingHoursToFinishTheTask;
	public boolean freeTask = true;
	
	
	public void setName(String name){
		if(name != null && !name.equals("")){
			this.taskName = name;
		}
	}
	
	public String getName(){
		return taskName;
	}
	
	public void setWorkingHoursToFinishTheTask(int workingHours){
		if(workingHours >= 0){
			this.workingHoursToFinishTheTask = workingHours;
		}
	}

	public int getWorkingHoursToFinishTheTask(){
		return workingHoursToFinishTheTask;
	}
	
	public Task(String name, int workingHours){
		setName(name);
		setWorkingHoursToFinishTheTask(workingHours);
	}
	
}

