package EmplyeeTaks;

public class Employee {
	private String name;
	Task currentTask;
	private int hoursLeft;

	Employee(String name) {
		if (name != null) {
			this.name = name.trim();
		} else
			this.name = "";
		if (currentTask == null) {
			currentTask = new Task();
		}
	}

	public void work() {
		if (hoursLeft != 0) {
			if (hoursLeft > currentTask.getWorkingHours()) {
				hoursLeft = hoursLeft - currentTask.getWorkingHours();
				currentTask.setWorkingHours(0);
			} else if (hoursLeft == currentTask.getWorkingHours()) {
				hoursLeft = 0;
				currentTask.setWorkingHours(0);
			} else {
				currentTask.setWorkingHours(currentTask.getWorkingHours() - hoursLeft);
				hoursLeft = 0;
			}
			showReport();
		} else {
			System.out.println(this.name + " veche si e izrabotil chasovete rabota");
		}
	}

	public void showReport() {
		System.out.println(name + " raboti po " + currentTask.getName() + " ostava mu da raboti " + hoursLeft
				+ " a ostavat " + currentTask.getWorkingHours() + " chasa rabota");
	}

	public Task getCurrentTask() {
		return currentTask;
	}

	public void setCurrentTask(Task currentTask) {
		if (currentTask == null) {
			this.currentTask = new Task();
			return;
		}
		this.currentTask = currentTask;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) {
			this.name = name.trim();
		}
	}

	public int getHoursLeft() {
		return hoursLeft;
	}

	public void setHoursLeft(int hoursLeft) {
		if (hoursLeft >= 0) {
			this.hoursLeft = hoursLeft;
		}
	}

}

