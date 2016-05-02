package ce1002.s101502020;

import java.util.Scanner;

public class Q1 {
	
	/**
	 * @param args
	 */
	public static double toFahrenheit (double c, double f){
		f=c*9/5+32 ;
		return f ;
	}		//do the calculation about c=>f
	
	public static double toCelsius(double c, double f){
		c=(f-32)*5/9 ;
		return c ;
	}		//do the calculation about f=>c
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
				
		System.out.println("Please choose the method you want to use:");
		System.out.println("1.toFahrenheit");
		System.out.println("2.toCelcius");
		System.out.println("3.Exit");
		
		//basic imformation print by instructions above
		
		Scanner input = new Scanner(System.in) ;
		
		int way=input.nextInt() ; //method to choose 1~3
		double f=0 , c=0 ;	//f for Fahrenheit ; c for Celcius
		
		
		if (way==1)		//to Fahrenheit
		{
			System.out.println("Please input the temperature: ");
			c=input.nextDouble() ;
			f=toFahrenheit(c,f) ;
			System.out.println( c + " in Celcius is equal to "+ f + " in Fahrenheit." );
		}
		else if (way==2)	//to Celsius
		{
			System.out.println("Please input the temperature: ");
			f=input.nextDouble() ;
			c=toCelsius(c,f) ;
			System.out.println( f + " in Fahrenheit is equal to " + c + " in Celcius." );
		}
		else if (way==3)	//ending
		{
			System.out.println("Good Bye") ;
		}
		
	} 
	
} 


