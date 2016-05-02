package ce1002.s101502514;
import java.util.Scanner;

public class Q1 {
	
	public static double toFahrenheit(double b){//?????
		return b=b*9/5+32;
	}
	public static double toCelsius(double c){//?????
		return c=(c-32)*5/9;
	}
	public static void main(String[] args){
		while(true){
			
			System.out.println("Please choose the method you want to use:");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit");
			Scanner input=new Scanner(System.in);
			double a=input.nextDouble();//??
				
			if(a==1){
				
				System.out.println("Please input the temperature:");
				double t=input.nextDouble();//????
				System.out.println(t+" in Celcius is equal to "+toFahrenheit(t)+" in Fahrenheit.");	
				System.out.println(" ");
				
			}
			
			if(a==2){
				
				System.out.println("Please input the temperature:");
				double f=input.nextDouble();//????
				System.out.println(f+" in Fahrenheit is equal to "+toCelsius(f)+" in Celcius.");	
				System.out.println(" ");
			}
			if(a==3){
				
				System.out.println("Good Bye");
				break;
					
			}
			
		}
						
	}
	
}

