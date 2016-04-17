package practice1;

public class FooCorporation {
	public static void main (String[] arguments){
		double minimumWage = 8.00;
		int maxHours = 60;
		salaryCalculation(7.5,37, minimumWage, maxHours);
		salaryCalculation(8.2,47, minimumWage, maxHours);
		salaryCalculation(10.0,73, minimumWage, maxHours);
	}
	public static void salaryCalculation
	(double basePay, int hoursWorked, double minimumWage, int maxHours){
		double totalSalary = 0;
		if ((basePay < minimumWage) || (hoursWorked > maxHours)){
			System.out.println("Error!");
		}
		else {	
			if (hoursWorked > 40){
				totalSalary = basePay * 40 + 1.5*basePay*(hoursWorked - 40);
			}
			else {
				totalSalary = basePay * hoursWorked;
			}
			System.out.println("Your total salary is " + totalSalary);
		}
	}
	
}

