package lesson16_zad01_homework2;

public class Task {

	private String name;
	private double workingHours;
	
	public Task(String name, double workingHours){
		if(name != null){
			this.name = name;
		}
		else {
			this.name = "";
		}
		
		if(workingHours > 0){
			this.workingHours = workingHours;
		}
		else {
			this.workingHours = 0;
		}
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setName(String name){
		if(name != null){
			this.name = name;
		}
		else {
			this.name = "";
		}
	}
	
	public double getWorkingHours(){
		return this.workingHours;
	}
	
	// Verifying input data
	public void setWorkingHours(double workingHours){
		if(workingHours > 0){
			this.workingHours = workingHours;
		}
		else {
			this.workingHours = 0;
		}
	}
}

