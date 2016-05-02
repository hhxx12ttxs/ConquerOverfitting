package ce1002.s101502526;
import java.util.Scanner;
public class Q1 {
	public static void main(String[] args) {
		Scanner input=new Scanner(System.in);
		int x ; 
		for ( ;  ; )
		{
			System.out.println("Please choose the method you want to use:");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit ");
			x=input.nextInt();      
			if ( x == 1 )           //?X=1?????????????????
				Q1.toFahrenheit(args);
			else if ( x == 2 )      //?X=2?????????????????
				Q1.toCelcius(args);
			else if ( x == 3)       //?X=3??????
			{
				System.out.println("Good Bye");
				break;
			}
		}
	}

	public static void toFahrenheit(String[] args) {
		Scanner input2=new Scanner(System.in);
		double a =0 , b =0;
		System.out.println("Please input the temperature: ");
		a=input2.nextInt();  
		b = a*9/5 + 32 ;
		System.out.println(a + " in Celcius is equal to " + b  + " in Fahrenheit " );
		System.out.println("");
	}

	public static void toCelcius(String[] args) {
		Scanner input3=new Scanner(System.in);
		double a =0 , b =0;
		System.out.println("Please input the temperature: ");
		a = input3.nextInt();
		b = (a-32)*5/9 ;
		System.out.println(a + " in Fahrenheit is equal to " + b  + " in Celcius " );
		System.out.println("");
	}
}

