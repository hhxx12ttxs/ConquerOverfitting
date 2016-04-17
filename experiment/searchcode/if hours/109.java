
public class Employee {

	private String name;
	private Task currentTask;
	private double hoursLeft;
	
	Employee(String name) {
		
		this.name = name;
	}
	void work(){
		if(hoursLeft > currentTask.getWorkingHours()){
			System.out.println("Employee`s hours left : "+(hoursLeft-=currentTask.getWorkingHours()));
			currentTask.setWorkingHours(0);
			System.out.println("Current task hours left : "+(currentTask.getWorkingHours()));
			return;
		}
		if(hoursLeft < currentTask.getWorkingHours()){
			double hours = currentTask.getWorkingHours()-hoursLeft;
			currentTask.setWorkingHours(hours);
			System.out.println("Current task hours left : "+(currentTask.getWorkingHours()));
			hoursLeft = 0;
			System.out.println("Employee`s hours left : "+hoursLeft);
			return;
		}
		else{
			hoursLeft = 0;
			System.out.println("Employee`s hours left : "+hoursLeft);
			currentTask.setWorkingHours(0);
			System.out.println("Current task hours left : "+currentTask.getWorkingHours());
			return;
		}
		
	}
	
	
	
	void showReport(){
		System.out.println();
		System.out.println(name);
		System.out.println(currentTask.getName());
		work();
		System.out.println();
	}
	
	public String getName(){
		return name;
	}
	public void setName(String name){
		if(name != null){
			this.name=name;
		}
	}
	public Task getCurrentTask(){
		return currentTask;
	}
	public void setCurrentTask(Task currentTask){
		if(currentTask != null){
			this.currentTask=currentTask;
		}
	}
	public double getHoursLeft(){
		return hoursLeft;
	}
	public void setHoursLeft(double hoursLeft){
		if(hoursLeft>=0){
			this.hoursLeft=hoursLeft;
		}
	}
	
}

