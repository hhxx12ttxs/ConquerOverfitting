package ce1002.s101502016;

import java.util.*;



public class Q1 {
	
	public static void CtoF(double a)
	{
		double b;
		b = a*9/5+32;
		System.out.println(a+" in Celcius is equal to "+b+" in Fahrenheit");
	}
	public static void FtoC(double c)
	{
		double d;
		d = (c-32)*5/9;
		System.out.println(c+" in Fahrenheit is equal to "+d+" in Celcius");
	}
	
	
	public static void main(String[] args) 
	{
		int x;//used to choose
		double y,C,F;
		
		Scanner imput = new Scanner(System.in);
		
		System.out.println("Please choose the method you want to use:");
		System.out.println("1.toFahrenheit");
		System.out.println("2.toCelcius");
		System.out.println("3.Exit");
		
		x = imput.nextInt();
		
		if (x==1)
		{
			y = imput.nextDouble();
			CtoF(y);
		}
		
		if (x==2)
		{
			y = imput.nextDouble();
			FtoC(y);
		}
		
		if (x==3)
		{
			System.out.println("Good Bye");
		}
			
	}
}

