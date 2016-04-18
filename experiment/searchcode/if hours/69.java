/**
 * Author: Brandon B.
 * Date: 8-26-15
 */

import java.util.Scanner;

public class weeklyPay {
    
    public static void main(String[] args) {
    	final double normalHours = 40.0, overtimeMultiplier = 1.5;
    	final String seperator = "=====================================";

    	double wage, hours, overtimeHours, pay, regularPay, overtimePay;
    	double mondayHours, tuesdayHours, wednesdayHours, thursdayHours, fridayHours;
    	Scanner userInput = new Scanner(System.in);

		System.out.print("Enter the hourly wage: ");
		wage = userInput.nextDouble();

		System.out.print("Enter the hours worked [Monday]: ");
		mondayHours = userInput.nextDouble();
		
		System.out.print("Enter the hours worked [Tuesday]: ");
		tuesdayHours = userInput.nextDouble();
		
		System.out.print("Enter the hours worked [Wednesday]: ");
		wednesdayHours = userInput.nextDouble();
		
		System.out.print("Enter the hours worked [Thursday]: ");
		thursdayHours = userInput.nextDouble();
		
		System.out.print("Enter the hours worked [Friday]: ");
		fridayHours = userInput.nextDouble();
		
		hours = mondayHours + tuesdayHours + wednesdayHours + thursdayHours + fridayHours;
		if(hours > normalHours) {
			overtimeHours = hours - normalHours;
		}
		else {
			overtimeHours = 0;
		}

		regularPay = (hours - overtimeHours) * wage;
		overtimePay = overtimeHours * wage * overtimeMultiplier;
		pay = regularPay + overtimePay;

		System.out.println("\nRegular Work Hours: " + normalHours);
		System.out.println("Overtime Multiplier: " + overtimeMultiplier);
		System.out.println(seperator);
		System.out.println("Total Hours Worked: " + hours);
		System.out.println("Toal Overtime Hours: " + overtimeHours);
		System.out.println("\nGeneral Wage: $" + wage);
		System.out.println("Regular Pay: $" + regularPay);
		System.out.println("Overtime Pay: $" + overtimePay);
		System.out.println(seperator);
		System.out.println("Total Pay: $" + pay);
    }
}
