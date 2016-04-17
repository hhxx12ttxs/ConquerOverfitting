import java.text.DecimalFormat;

public class Payroll {
	private double wage;
	private int hours;
	private double salary;
	DecimalFormat df = new DecimalFormat("#.00");

	public Payroll(double w, int h) {
		wage = w;
		hours = h;
		salary = 0;
	}

	public String findSalary() {
		if (hours < 40) {
			salary = hours * wage;

		}

		else if (hours <= 48) {
			salary = 40 * wage + (hours - 40) * wage * 1.5;
		}

		else if (hours > 48) {
			salary = 40 * wage + 8 * wage * 1.5 + (hours - 48) * wage * 2;
		}
		return "Salary is $" + df.format(salary);
	}

}

