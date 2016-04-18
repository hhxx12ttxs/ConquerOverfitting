package TaskAndEmployee;

public class Task {

	private String name;
	private int workingHours;
	
	
	public Task(String name, int workingHours){
		
		if(this.name != " " && isLetter(name)){
			this.name = name;
		}
		
		if(this.workingHours >= 0)
			this.workingHours = workingHours;
		
	}
	
	private boolean isLetter(String s){
			
			boolean flag = true;
			int i = 0;
			while(i < s.length()){
			     if(s.charAt(i) >='a' && s.charAt(i) <= 'z' || s.charAt(i) >='A' && s.charAt(i) <= 'Z' || s.charAt(i) == ' ')
			    	 i++;
			     else{
			    	 flag = false;
			    	 break;
			     }
			     
			}
			return flag;
		}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		this.workingHours = workingHours;
	}
}

