package hw07;

public class Task {

	// attributes

	private String name;
	private int workingHours;

	// constructor

	Task(String name, int workingHours) {
		setName(name);
		setWorkingHours(workingHours);
	}

	// methods

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		if (name != null && !name.equals("")) { //!! prowerqwame dali potebitelq nqma da wywede null ili prazen niz
			//if(!"".equals(name)) {
			this.name = name;
		}
		else {
			System.out.println("Bad input");
		}

	}

	public int getWorkingHours() {
		return this.workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		}
	}
}

