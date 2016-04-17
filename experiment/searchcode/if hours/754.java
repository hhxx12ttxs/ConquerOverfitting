
public class Task {
	
	private String name;
	private int hours; 
	
	Task(){
	}
	
	Task(String name, int hours){
		if((name.equals("")) || name.matches(".*\\d+.*" ) || name.equals(" ") || name.trim().length() < 2){
			System.out.println("Invalid input. Only letters are allowed when defining a task.");
		}else{
			this.name = name;
		}
		if(hours <= 0){
			System.out.println("Invalid working hours input.");
		}else{
			this.hours = hours;
		}
	}	
	
	public int getHours(){
		return hours;
	}
	
	public void setHours(int hours){
		if(hours < 0){
			System.out.println("Invalid working hours input.");
		}else{
			this.hours = hours;
		}
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		if((name.equals("")) || name.matches(".*\\d+.*" ) || name.equals(" ") || name.trim().length() < 2){
			System.out.println("Invalid input. Only letters are allowed when defining a task.");
		}else{
			this.name = name;
		}
	}

}

