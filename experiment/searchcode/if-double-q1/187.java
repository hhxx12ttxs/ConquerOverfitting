package ce1002.s101502012;
import java.util.Scanner;
public class Q1 {
	public static double toFahrenheit (double Celcius) // toFahrenheit Fuction
	{
		double toFahrenheit;
		toFahrenheit = Celcius*9/5 + 32; // The formula is for converting Celcius to Fahrenheit
		return toFahrenheit;
	}
	public static double toCelcius (double Fahrenheit) // toCelcius Fuction
	{
		double toCelcius;
		toCelcius=(Fahrenheit-32)*5/9; // The formula is for converting Fahrenheit1 to Celcius
		return toCelcius;
	}
	public static void main(String[] agrs) // Main Fuction
	{
		while(true)
		{
			Scanner input = new Scanner(System.in); // Change Scanner to System.in
			System.out.println("Please choose the method you want to use: \n1.toFahrenheit \n2.toCelcius \n3.Exit ");
			int answer;
			answer = input.nextInt();
			if (answer==1)
			{ 
				System.out.println("Please input the temperature: ");
				double Celcius;
				Celcius = input.nextDouble();
				System.out.print (+Celcius);
				System.out.print (" in Celcius is equal to ");
				System.out.print ( +toFahrenheit(Celcius)); // Call funtion toFahrenheit
				System.out.println (" Fahrenheit.\n");
			}
			else if (answer==2)
			{
				System.out.println("Please input the temperature: ");
				double Fahrenheit;
				Fahrenheit = input.nextDouble();
				System.out.print (+Fahrenheit);
				System.out.print (" in Fahrenheit is equal to "); // Call funtion toCelcius
				System.out.print (+toCelcius (Fahrenheit));
				System.out.println (" in Celcius.");
			}
			else
			{
				System.out.println("Good Bye");
				break; //exit
			}
		}
	
	}
}

