
public class Employee {
	private String name;
	private Task currentTask;
	private int hoursLeft;

	public Employee(String name) {
		this.setName(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name == null) {
			throw new NullPointerException();
		}
		this.name = name;
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public boolean setCurrentTask(Task currentTask) {
		if (currentTask == null) {
			return false;
		} else {
			this.currentTask = currentTask;
			return true;
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public boolean setHoursLeft(int hoursLeft) {
		if (hoursLeft < 0) {
			return false;
		}
		this.hoursLeft = hoursLeft;
		return true;
	}

	public void work() {
		int min = Math.min(hoursLeft, currentTask.getWorkingHours());
		hoursLeft -= min;
		currentTask.setWorkingHours(currentTask.getWorkingHours() - min);
	}

	public void showReport() {
		System.out.println(name + " " + currentTask.getName() + " " + hoursLeft + " " + currentTask.getWorkingHours());
	}
}

