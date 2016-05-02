package ce1002.s101502527;
import java.util.Scanner;
public class Q1 {
	public static void main(String[] args){
		Scanner scan = new Scanner(System.in);
		int x;
		double y;
		do{
			System.out.println("Please choose the method you want to use:"); //output choices
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit");
			
			x=scan.nextInt();  //input x for your choice
			if(x==3)//end if x = 3
				break;
			System.out.println("Please input the temperature:");// output question
			y=scan.nextDouble();// input y as temperature
			if(x==1){//c to f call function and output
				System.out.println(y+" in Celcius is equal to "+toFahrenheit(y)+" in Fahrenheit.");
			}
			else//f to c call function and output
				System.out.println(y+ " in Fahrenheit is equal to "+toCelcius(y)+" in Celcius.");
			
			System.out.println();//empty line
			
		}while(true);
		
		System.out.print("Good Bye");//end program
		
	}
	
	public static double toFahrenheit(double c){
		return c*9/5+32;
	}
	
	public static double toCelcius(double f){
		return (f-32)*5/9;
	}

}

