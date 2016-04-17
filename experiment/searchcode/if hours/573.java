package salaryCalculator;

import java.io.*;
import java.text.*;

public class Employee {
	//declare variables
	double minimumPay = 8.0;
	int maximumHours = 60;
	double overtimeRate = 1.5;
	int maxBaseHours = 40;
				
	public String name;
	public double basePay;
	public int numHoursWorked;
	
	//class constructor	
	public Employee(String n, double base, int numHours) {
		name = n;
		basePay = base;
		numHoursWorked = numHours;
	}
	
	//get value methods
	double getMinPay(){
			return minimumPay;
		}
		
	double getMaxHours(){
			return maximumHours;
		}
				
	double getOvertimeRate(){
			return overtimeRate;
		}
				
	double getMaxBaseHours(){
			return maxBaseHours;
		}
	
    DecimalFormat moneyFormat = new DecimalFormat("0.00");

	//method to calculate and display total pay for each employee by name			
	void calculateTotalPay() {
		if (basePay >= getMinPay() & numHoursWorked <= getMaxHours())
			
			if (numHoursWorked <= getMaxBaseHours())
				System.out.println("Total pay for " + name + " is: $" +  moneyFormat.format(basePay * numHoursWorked));
			else System.out.println("Total pay for " + name + " is: $" +  moneyFormat.format(((basePay * getMaxBaseHours()) + (basePay * getOvertimeRate()) * (numHoursWorked - getMaxBaseHours()))));
		else if (basePay < getMinPay() & numHoursWorked > getMaxHours())
			System.out.println("The number of hours entered is too high and the base pay entered is too low for " + name);
		else if (basePay < getMinPay() & numHoursWorked <= getMaxHours())
			System.out.println("The base pay entered is too low for " + name);
		else 
			System.out.println("The number of hours entered is too high for " + name);
	}

}

