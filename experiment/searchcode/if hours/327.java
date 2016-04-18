package PackageWithoutEncapsulation;

public class Employee {
	String name;
	Task currentTask;
	int hoursLeft;
	
	Emplooyee(String name){
		if(name != null && !name.equals("")){
			this.name = name;
		}
		else{
			this.name = "Invalid name";
		}
	}
	
	void work(){
		if(this.hoursLeft<this.currentTask.workingHours){
			this.currentTask.workingHours = this.currentTask.workingHours-this.hoursLeft;
			this.hoursLeft = 0;
		}
		if(this.hoursLeft>=this.currentTask.workingHours){
			this.hoursLeft = this.hoursLeft - this.currentTask.workingHours;
			this.currentTask.workingHours = 0;
		}
	}
	
	void showReport(){
		System.out.println("Emplooyee name:" + this.name);
		System.out.println("Name of the task for " + this.name + " is:" + this.currentTask.name);
		System.out.println("Working hours left for the worker:" + this.hoursLeft);
		System.out.println("Working hours left for the task to be finished:" + this.currentTask.workingHours);
		System.out.println();
	}
}

