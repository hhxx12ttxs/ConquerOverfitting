
public class Task {
	private String name;
	private float hours;
	
	Task(final String name,final float hours){
		this.setName(name);
		this.setHours(hours);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		if(name!=null){
			this.name = name;
		}
		else{
			this.name="Unnamed";
		}
	}
	
	public float getHours() {
		return hours;
	}
	
	public void setHours(float hours) {
		if(hours>=0){
			this.hours = hours;
		}
		else{
			this.hours=0;
		}
		
	}
	
	
}

