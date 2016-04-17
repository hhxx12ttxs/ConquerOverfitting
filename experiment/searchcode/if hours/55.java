package task1;

public class Task {
	private String name;
	private double workingHours;

	Task(){
		name="Next task";
		workingHours=0;
	}
	protected void setName(String name) {
		if (!((name.equals("")) && (name.equals(null)))) {
			this.name = name;
		}
	}
	protected String getName() {
		return name;
	}
	protected void setWorkingHours(double workingHours) {
		if (workingHours>=0) {
			this.workingHours = workingHours;
		}
	}
	protected double getWorkingHours() {
		return workingHours;
	}
}

