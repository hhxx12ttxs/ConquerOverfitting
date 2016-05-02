package ce1002.s101502525;

import java.util.Scanner;

public class Q1 {
	static Scanner input=new Scanner(System.in);
	static int choose;
	public static void main(String[] args){
		do{
			System.out.println("Please choose the method you want to use:");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit");

			choose=input.nextInt();//input
			if(choose==1)//choose method
				toFahrenheit();
			else if(choose==2)
				toCelcius();
		}while(choose==1||choose==2);//if choose is not 1 and not 2, exit.
		System.out.println("Good Bye");
	}
	private static void toFahrenheit(){
		System.out.println("Please input the temperature:");
		double C=input.nextDouble();//input
		System.out.println(C+" in Celcius is equal to "+(C*9/5+32)+" in Fahrenheit.\n");//compute & output
	}
	private static void toCelcius(){
		System.out.println("Please input the temperature:");
		double F=input.nextDouble();//input
		System.out.println(F+" in Fahrenheit is equal to "+(F-32)*5/9+" in Celcius.\n");//compute & output
	}
}

