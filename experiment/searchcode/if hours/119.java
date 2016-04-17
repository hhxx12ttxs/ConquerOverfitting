package hw07;

public class Task {
	private String name;
	private  int workingHours;
	
	Task(String name,int hours){
		if(name.equals("")||hours>18){
			System.out.println("Please,enter correct data:");
		}else{
			this.name=name;
			this.workingHours=hours;
		}
	}
	public void setName(String name){
		this.name=name;
	}
	public String getName(){
		return name;
	}
	 public void  setWorkingHours(int workingHours){
		this.workingHours=workingHours;
	 }
	 public int getWorkingHours(){
		 return workingHours;
	 }
	 
}
