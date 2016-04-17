package Exercise_6_8;

import java.util.Scanner;

public class calculateCharges {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Enter parking hours:");
		Scanner s = new Scanner(System.in);
		int hours = s.nextInt();
		System.out.println("Your parking fee is: "+calculateCharges(hours));
	}
	
	
	private static double calculateCharges(int hours){
		if (hours<=3){
			return 2;
		}
		else if(hours >=24){
			return 10;
		}
		else{
			return  2+0.5*hours;
		}
	}

}

