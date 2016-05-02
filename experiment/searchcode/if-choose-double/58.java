package a1.s100502007;

import java.util.Scanner; 

public class A12 {
	public static double toFahrenheit(double celsius){//??????method
		double fahrenheit = ((9.0/5)*celsius)-32;
		return fahrenheit;
	}
	public static double toCelisus(double fahrenheit){//??????method
		double celsius = (5.0/9)*(fahrenheit-32);
		return celsius;
	}
	
	public static void main (String[] args){
		Scanner input = new Scanner(System.in);
		for( ; ; ){
		System.out.print("Welcome to use this method.\n"+"1.toFahrenheit: changes user input from Celsius to Fahrenheit.\n"+"2.toCelisus: changes user input from Fahrenheit to Celsius.\n"+"3.exit\n");//????3
		int choose = input.nextInt();;//????
		switch(choose){
			case 1:
				double Celsius = input.nextDouble();//??????????
				System.out.print("Celsius "+Celsius+" is "+toFahrenheit(Celsius)+" in Fahrenheit\n");
				break;
			case 2:
				double Fahrenheit = input.nextDouble();//??????????
				System.out.print("Fahrenheit "+Fahrenheit+" is "+toCelisus(Fahrenheit)+" in Celsius\n");
				break;
			case 3:
				break;
		}
		if(choose == 3){//????
			break;
		}
		}
	}

		
		
}

