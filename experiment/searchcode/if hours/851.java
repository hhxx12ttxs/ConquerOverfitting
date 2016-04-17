

public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;
	Employee(String name){
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		if(name.length() > 0 && name.charAt(0) != ' '){
			this.name = name;
			}	else {
				System.out.println("Input propper name");
			}
	}
	public Task getCurrentTask() {
		return currentTask;
	}
	public void setCurrentTask(Task currentTask) {
		if(currentTask != null){
		this.currentTask = currentTask;
		}	else {
			System.out.println("Not a valid task");
		}
	}
	public int getHoursLeft() {
		return hoursLeft;
	}
	public void setHoursLeft(int hoursLeft) {
		if(hoursLeft > 0){
		this.hoursLeft = hoursLeft;
		}	else {
			System.out.println("Input valid hours");
		}
	}
	public void work(){
		if(currentTask != null){
			if(hoursLeft < currentTask.getWorkingHours()){
				currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursLeft);
				hoursLeft = 0;
			}	else {
				hoursLeft -= currentTask.getWorkingHours();
				currentTask.setWorkingHours(0);
			}
			report();
		}	else {
			System.out.println("No Task");
		}
	}
	public void report(){
		System.out.println("Name of worker " + this.name + "\nName of worker's task " +
	currentTask.getName() + "\nworking hours left for worker " + this.hoursLeft + 
	"\nhours left for the task to be done " + currentTask.getWorkingHours());
	}
	
	
}

