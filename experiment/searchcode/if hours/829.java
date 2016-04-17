package edu.payroll.piland.will;

import java.text.NumberFormat;

import edu.jenks.dist.payroll.AbstractPayrollCalculator;

public class PayrollCalculator extends AbstractPayrollCalculator {

	@Override
	public double calculatePay(double hoursWorked) {
		
		if (hoursWorked >=40){
			hoursWorked = 40 + (hoursWorked - 40)*1.5;
		}
		double pay = hoursWorked * 10.5;
		return pay;
	}

	@Override
	public double convertToHours(int hours, int minutes, int seconds) {
		
		double hoursWorked = hours + ((double)minutes/60.0) + (double)seconds/3600.0;
		
		return hoursWorked;
	}

	@Override
	public String formatPay(double pay) {
		
		
		
		
		return NumberFormat.getCurrencyInstance().format(pay);
	}

	public static void main(String[] args) {
		

	}

}

