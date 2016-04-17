package edu.payroll.collier.serenity;

import java.text.NumberFormat;

import edu.jenks.dist.payroll.AbstractPayrollCalculator;

public class PayrollCalculator extends AbstractPayrollCalculator {
	double finalpay=0;

	@Override
	public double calculatePay(double hoursWorked) { 
		if (hoursWorked<=40)
			finalpay = (hoursWorked*10.5);
		
			else if (hoursWorked>40)
			finalpay = ((hoursWorked-40)*15.75 + (420));
			
		
		return finalpay;
	}

	@Override
	public double convertToHours(int hours, int minutes, int seconds) {
		double Hours=(hours+(minutes/60)+(seconds/3600)); 
		return Hours;
	}

	@Override
	public String formatPay(double pay) {
		NumberFormat.getCurrencyInstance().format(finalpay);
		System.out.println(finalpay);
		return null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}

