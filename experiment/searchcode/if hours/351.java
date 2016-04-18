package lesson08;

public class Task {

	// attributes

	private String name;
	private int workingHours;

	// constructor

	Task(String name, int workingHours) {
			this.setName(name);
			this.setWorkingHours(workingHours);
		}
	

	// methods

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) { //!! prowerqwame dali potebitelq nqma da wywede null ili prazen niz
			this.name = name;
		}
		else {
			System.out.println("Bad input for task name!");
		}

	}

	public int getWorkingHours() {
		return this.workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		}
		else{
			System.out.println("Bad input for working hours!");
		}
	}
}

