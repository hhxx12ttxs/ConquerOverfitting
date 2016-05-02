package ce1002.s101502516;

import java.util.Scanner; 

public class Q1 {
	private static double c,f;
	public static double toFahrenheit(double args){ //?????
		double a;
		a = c * 9 / 5 + 32;
		return a;
	}
	public static double toCelcius(double args){  //?????
		double b;
		b = ( f  -32) * 5 / 9;
		return b;
	}
	public static void main (String[] args){
		while(true){
			System.out.println("Please choose the method you want to use:");//????
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit ");
	
			Scanner input = new Scanner(System.in);//??
			int a;
			a = input.nextInt();
			
			if ( a == 1 ){ //??1???
				System.out.println("Please input the temperature:");
				Scanner input2 = new Scanner(System.in);
				c = input2.nextDouble();
				System.out.println(c + " in Celcius is equal to " + Q1.toFahrenheit(c) + " in Fahrenheit");
				System.out.println(" ");
			}
			else if ( a == 2 ){ //??2???
				System.out.println("Please input the temperature:");
				Scanner input2 = new Scanner(System.in);
				f = input2.nextDouble();
					System.out.println(f + " in Fahrenheit is equal to " + Q1.toCelcius(f) + " in Celcius");
					System.out.println(" ");
			}		
			else if ( a == 3 ){//??3???
				System.out.println("Good Bye");
				break; //????
			}
		}
	}
}

