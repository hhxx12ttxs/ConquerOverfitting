package a6.s100502511;

public class Employee extends Person {
	private double salary;
	private double rank;

	Employee() {

	}

	public Employee(String name, String gender, double newsalary, double newrank) {
		super(name, gender); // ??Person?name?gender
		this.salary = newsalary; // ??salary????newsalary
		this.rank = newrank; // ??rank????newrank
	}

	public double getSalary() { // ??salary
		return salary;
	}

	public void getRank() { // ??rank??
		if (this.rank == 1) {
			System.out.println("Professor");
		} else if (this.rank == 2) {
			System.out.println("Associated Professor");
		} else if (this.rank == 3) {
			System.out.println("Professor");
		} else if (this.rank == 4) {
			System.out.println("Lecturer");
		}
	}

	public String toString() { // ?????
		return "This guy is a Employee!!";
	}

	public String toString(boolean check) {
		String word = "";
		if (check == true) {
			word = "This guy is a Employee!!";
			return word;
		} else if (check == false) {
			word = "Though this guy is not a Student, This guy is a Person!! ";
			return word;
		} else
			return word;
	}
}

