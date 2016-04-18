public class Task {

	private String name;
	private int workingHours;

	Task(String name, int workingHours) {
		if (name.equals("")) {
			System.out.println("Invalid name");
		} else {
			this.name = name;
		}
		
		if (workingHours > 0) {
			this.workingHours = workingHours;
		} else {
			System.out.println("Invalid working hours!");
		}

	}

	public String getName() {
		return name;
	}

//	public void setName(String name) {
//		if (name.equals(" ")) {
//			System.out.println("Invalid name");
//		} else {
//			this.name = name;
//		}
//	}

	public int getWorkingHours() {
		return workingHours;
	}

	public void setWorkingHours(int workingHours) {
		if (workingHours >= 0) {
			this.workingHours = workingHours;
		} else {
			System.out.println("Invalid working hours!");
		}
	}

}

