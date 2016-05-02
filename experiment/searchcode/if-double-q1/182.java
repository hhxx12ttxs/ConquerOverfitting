//Q1-100502201
//An easy transform of Celcius and Fahrenheit
package ce1002.s100502201;
import java.util.Scanner;

public class Q1 {
	public static void main(String[] args){ 
		while(true){ //Loop of program until exit
			System.out.println("Please choose the method you want to use:");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit");
			Scanner scanner = new Scanner(System.in); //Construct a scanner
			String input;
			input = scanner.next(); //input
			if (input.equals("1")){ //Cel to Fah
				System.out.println("Please input the temperature:");
				Scanner cel = new Scanner(System.in);
				double tmp=cel.nextDouble(); //Receive input as double
				double res=tmp*9/5+32;
				System.out.printf(tmp + " in Celcius is equal to " + res + " in Fahrenheit\n\n");
			}
			else if (input.equals("2")){ //Fah to Cel
				System.out.println("Please input the temperature:");
				Scanner fah = new Scanner(System.in);
				double tmp1=fah.nextDouble(); //Receive input as double
				double res1=(tmp1-32)*5/9;
				System.out.printf(tmp1 + " in Fahrenheit is equal to " + res1 + " in Celcius\n\n");
			}
			else { //Break
				System.out.println("Goodbye");
				break;
			}
		}
	}
}


