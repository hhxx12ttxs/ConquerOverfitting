
public class Employee {
	private String name;
	private Task currentTask;
	private float hoursLeft;

	public Employee(final String name) {
		this.setName(name);
		this.setCurrentTask(null);
		this.setHoursLeft(0f);
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

	public Task getCurrentTask() {
		if(this.currentTask!=null){
			return currentTask;
		}
		else{
			return new Task("No Task",0f);
		}
	}

	public void setCurrentTask(Task currentTask){
		if(currentTask!=null){
			this.currentTask=currentTask;
		}
		else{
			this.currentTask=new Task("No Task",0f);
		}
	}

	public float getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(float hoursLeft) {
		if(hoursLeft>=0){
			this.hoursLeft = hoursLeft;
		}
		else{
			this.hoursLeft=0;
		}
	}
	
	public void work(){
		if(this.currentTask.getHours()>this.hoursLeft){
			this.currentTask.setHours(this.currentTask.getHours()-this.hoursLeft);
			this.hoursLeft=0;
		}
		else if(this.currentTask.getHours()<this.hoursLeft){
			this.hoursLeft-=this.currentTask.getHours();
			this.currentTask.setHours(0);
		}
		else{
			this.currentTask.setHours(0);
			this.hoursLeft=0;
		}
		showReport(this);
	}
	
	private static void showReport(final Employee worker){
		System.out.println("Name: " + worker.getName());
		System.out.println("Task' name: " + worker.getCurrentTask().getName());
		System.out.println("Employee' hours left: " + worker.getHoursLeft());
		System.out.println("Current Task' hours left: " + worker.getCurrentTask().getHours());
		System.out.println("");

	}
	
}

