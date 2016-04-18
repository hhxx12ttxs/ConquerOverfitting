package office;

public class Task {
	
	//Fields:
	private String name; 
	private int workingHours; 
	
	//Constructor:
	public Task(String name, int workingHours) {  
		
		if (name != null) {
			this.name = name;
		} else {
			System.out.println("NoNameError");
			return;
		}
		if (workingHours > 0) {
			this.workingHours = workingHours;
		}
	}
	
	//Setter:
	public void setWorkingHours(int workingHours) { 
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		}
	}
	
	
		
	//Getters:
	public String getName() { 
		return this.name;
	}
	
	public int getWorkingHours() { 
		return this.workingHours;
	}
	
}

