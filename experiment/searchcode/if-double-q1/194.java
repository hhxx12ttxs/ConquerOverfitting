package ce1002.s101502509;
import java.util.Scanner;

public class Q1 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int choice = 0;
		do{
			System.out.println("Please choose the method you want to use: ");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit ");
			Scanner input = new Scanner(System.in);
			choice=input.nextInt();        // Get user's choice
			
			switch(choice){            
				case 1 :{                  // If choice == 1,convert celcius to fahrenheit
					System.out.println("Please input the temperature:");
					double celcius =input.nextDouble();
					celciusToFahrenheit(celcius);
					break;
				}
				case 2 :{                  //If choice == 2,convert fahrenheit to celcius
					System.out.println("Please input the temperature:");
					double fahrenheit =input.nextDouble();
					fahrenheitToCelcius(fahrenheit);
					break;
				}
				case 3 :{                  //If choice == 3,exit the program
					System.out.println("Good Bye");
					break;
				}
			}		
		}while(choice != 3);		
	}
	public static void celciusToFahrenheit(double num1){
		double fnum1;
		fnum1 = num1 *(9.0/5) + 32;      //F = C * (9/5) + 32
		System.out.println(num1+" in Celcius is equal to "+fnum1+" in Fahrenheit.");
		System.out.println();
	}
	public static void fahrenheitToCelcius(double num2){
		double cnum2;
		cnum2 = (num2-32)*5/9;           //C = (F ?V 32) * 5 / 9
		System.out.println(num2+" in Fahrenheit is equal to "+cnum2+" in Celcius.");
		System.out.println();
	}
 
}

