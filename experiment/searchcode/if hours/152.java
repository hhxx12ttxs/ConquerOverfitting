
public class Task {
	private final int MAX_WORKING_HOURS = 8;
	private final int MIN_WORKING_HOURS = 0;
	
	private String name;
	double workingHours;
	
	public void setName (String newName){
		if (newName!=null && !newName.equals("")){
			this.name=newName;
		}
		
	}
	
	public void setHours (double newWorkingHours){
		if (newWorkingHours>=MIN_WORKING_HOURS && newWorkingHours<=MAX_WORKING_HOURS){
			this.workingHours=newWorkingHours;
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	Task(String newName, double newWorkingHours){
		setName(newName);
		setHours(newWorkingHours);
	}
	

}

