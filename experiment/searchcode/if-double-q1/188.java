package ce1002.s101502002;

import java.util.Scanner;
public class Q1 {
	public static void main(String[]args){ 
		int method=0 ;//??
		while (method!=3)//???????????3??????
		{
			System.out.println("Please choose the method you want to use:\n1.toFahrenheit\n2.toCelcius\n3.Exit");//????
			Scanner input =new Scanner(System.in);
			method =input.nextInt();//????
			if (method==1)//??1
			{
				System.out.println("Please input the temperature:");
				double i=input.nextDouble();//??????
				System.out.println(i+" in Celcius is equal to "+toFahrenheit(i) +" in Fahrenheit.\n");//???????
			}
			else if (method ==2)//??2
			{
				System.out.println("Please input the temperature:");
				double i=input.nextDouble();
				System.out.println(i+" in Fahrenheit is equal to "+toCelcius(i) +" in Celcius.\n");
			}
			else//??3
			{
				System.out.println("Good Bye");
				break;//????
			}
		}
	
	}
	public static double toFahrenheit(double a){//??????
		double answer =a*9/5+32;
		return answer;//??????
	}
	public static double toCelcius(double a){//??????
		double answer = (a-32)*5/9;
		return answer;//??????
	}
	

	

}

