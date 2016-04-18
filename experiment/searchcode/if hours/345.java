package workStatistic;

public class Task {
	private String name;
	private int workHours;
	
	public Task(String name, int workHours){
		this.setName(name);
		this.setWorkHours(workHours);
	}
	
	public void setName(String name){
		if (name.length() < 2) {
			System.out.println("Name of the task must be at least 2 characters long!");
		} 
		else {
			this.name = name;
		} 
	}
	
	public String getName(){
		return this.name;
	}
	
	public void setWorkHours(int hours){
		if (hours < 0) {
			System.out.println("Working hours must be non negative!");
		}
		else {
			this.workHours = hours;
		}
	}
	
	public int getWorkHours(){
		return this.workHours;
	}
}

