package com.ctu.dietel.chapter6;

import java.util.Scanner;

public class ParkingCharges {

	public static void main(String[] args) {

		double totalReceipts = 0;
		double charge;
		double hours;

		Scanner input = new Scanner(System.in); // user will enter number of hours parked
		System.out.print("Enter number of hours parked(-1 to quit): ");
		hours = input.nextDouble();
		
		while(hours >= 0.0){
			
			if(hours > 24.0){
				
				System.out.println("We do not have facility to park more than 24 hours at a time. Please enter less than or equal to 24 hours");
				System.out.print("Enter number of hours parked(-1 to quit): ");
				hours = input.nextDouble();
				
			}else{
				
				charge = calculateCharges(hours);
				totalReceipts = totalReceipts+charge;
				System.out.printf("Current Charge: $%.2f", charge);
				System.out.printf(" Total Reciepts: $%.2f\n", totalReceipts);
				System.out.print("Enter number of hours parked(-1 to quit): ");
				hours = input.nextDouble();
			}
		}

	}

	public static double calculateCharges(double hours) {

		double charge = 2.0; // minimum charge for 3 hours has $2.00

		if (hours > 3.0){
			charge = 2.0 + 0.5 * Math.ceil(hours - 3.0); //  calculate extra charge if it was parked more than 3 hours
		}
		if (hours >= 19.0 && hours <= 24.0){
			charge = 10.0; // if hours is equal or greater than 19 and less than or equal to 24 then maximum charge will be $10
		}
		return charge;
	} 

}


