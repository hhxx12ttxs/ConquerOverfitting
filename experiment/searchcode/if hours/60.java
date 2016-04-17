
public class Task {

   	 private String name;
	 private double workingHours;
	
	Task(){
		setName("Cleaner");
		setWorkingHours(8);
	}
	

	public String getName(){
		return name;
	}
	
	public void setName(String name){
		if(name != null){
			this.name = name;
		}
	}
	
	public double getWorkingHours(){
		return workingHours;
	}
	
	public void setWorkingHours(double workingHours){
		if(workingHours >= 0){
			this.workingHours = workingHours;
		}
	}
	
}

