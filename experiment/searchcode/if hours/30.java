
public class Task {
	String name;
	int workingHours;
	
	Task(String name, int workingHours){
		if(name != null && !name.equals("")){
			this.name = name;
		}
		else{
			this.name = "Invalid name";
		}
		
		if(workingHours>0){
			this.workingHours = workingHours;
		}
		else{
			this.workingHours = 0;
		}
	}
}


