package edu.payroll.ruhoff.brooke;

import java.text.NumberFormat;

import edu.jenks.dist.payroll.AbstractPayrollCalculator;

public class PayrollCalculator extends AbstractPayrollCalculator {
	double finalPay;
	double totalHours;
	@Override
	public double calculatePay(double hoursWorked) {
		totalHours=hoursWorked;
		if (hoursWorked<=40) {
			finalPay = (hoursWorked*10.5);
		}
		else if (hoursWorked>40){
			finalPay = (((hoursWorked-40)*15.75)) + (420);
		}
		return finalPay;
	}

	@Override
	public double convertToHours(int hours, int minutes, int seconds) {
		totalHours=(hours+(minutes/60)+(seconds/3600));
		return totalHours;
	}

	@Override
	public String formatPay(double pay) {
		pay=finalPay;
		NumberFormat.getCurrencyInstance().format(finalPay);
		System.out.println("$" + finalPay);
		return null;
	}

	public static void main(String[] args) {
		PayrollCalculator pc = new PayrollCalculator();
		pc.convertToHours(45, 65, 23);
	}


}

