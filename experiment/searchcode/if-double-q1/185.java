package ce1002.s101502009;

import java.util.Scanner;



public class Q1 {
	
	public static double toFahrenheit(double c1){
		c1=((c1*9/5)+32);
		double F1=c1;
		return F1;		
	}//end of toFahrenheit
	
	public static double toCelsius(double f2){
		f2=((f2-32)*5/9);
		double C2=f2;
		return C2;		
	}//end of toCelsius
	
	
	public static void main(String[] args){
		while(true){
		System.out.println("Please choose the method you want to use:");
		System.out.println("1.toFahrenheit");
		System.out.println("2.toCelcius");
		System.out.println("3.Exit");
		
		Scanner input= new Scanner(System.in);
		int x=input.nextInt();
		
				//c to f
				if(x==1){
					System.out.println("Please input the temperature:");					
					double c1=input.nextInt();
					double c=c1;					
					System.out.println(c+" in Celcius is equal to "+toFahrenheit(c1)+" in Fahrenheit.");					
				}
				
				//f to c
				if(x==2){
					System.out.println("Please input the temperature:");
					double f2=input.nextInt();
					double f=f2;
					System.out.println(f+" in Fahrenheit is equal to "+toCelsius(f2)+" in Celcius.");					
				}
				
				//goodbye
				if(x==3){
					System.out.println("Good Bye");
					return;					
				}
				
		}//end of while
	}//end of main

}//end of Q1

