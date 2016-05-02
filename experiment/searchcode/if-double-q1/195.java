package ce1002.s101502028;
import java.util.*;
public class Q1 {
	public static void main(String[] args){
		Scanner temperature = new Scanner (System.in); // scanner object
		do{ // do while loop
		int option;
		double temp, result = 0;
		System.out.println("Please choose the method you want to use:"); // print message
		System.out.println("1.toFahrenheit");
		System.out.println("2.toCelcius");
		System.out.println("3.Exit");
		option = temperature.nextInt();
		if (option == 1){ // first option
			System.out.println("Please input the temperature:");
			temp = temperature.nextInt(); // input the temp
			System.out.println(temp + " in Celcius is equal to " + toFahrenheit(result, temp) + " in Fahrenheit.");
			// print the result
			System.out.println();
		}
		if (option == 2){ // second option
			System.out.println("Please input the temperature:");
			temp = temperature.nextInt(); // input the temp
			System.out.println(temp + " in Fahrenheit is equal to " + toCelcius(result, temp) + " in Celcius.");
			//print the result
			System.out.println();
		}
		if (option == 3){ // finish the program
			System.out.println("Good Bye"); // print message
			break; // break loop
		}
		}while(true); // do while loop
		
	}
	static double toFahrenheit(double f, double c){ // change to fahrenheit function
		f = (c * 9 / 5 + 32);
		return f; // return value f
	} // end function
	
	static double toCelcius(double c, double f){ // change to celcius function
		c =  ((f - 32) * 5 / 9);
		return c; // return value c
	} // end function

} // end class

