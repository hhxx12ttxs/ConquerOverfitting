package ce1002.s101502524;
import java.util.Scanner;

public class Q1 {
	
	public static double toF(double T)//?Celcius??Fahrenheit
	{
		double T1;
		T1=T*9/5+32;
		return T1;
	}
	public static double toC(double T)//?Fahrenheit??Celcius
	{
		double T1;
		T1=(T-32)*5/9;
		return T1;
	}
	public static void main(String[] agrs)
	{
		while(true)//??????
		{
			Scanner input=new Scanner(System.in);
			int a;
			double T;
			System.out.println("Please choose the method you want to use:");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit");
			a=input.nextInt();
			if(a==1)//??1?????????
			{
				System.out.println("Please input the temperature:");
				T=input.nextDouble();
				System.out.println(T +" in Celcius is equal to "+ toF(T) +" in Fahrenheit.");
				System.out.println();
			}
			if(a==2)//??2?????????
			{
				System.out.println("Please input the temperature:");
				T=input.nextDouble();
				System.out.println(T +" in Fahrenheit is equal to "+ toC(T) +" in Celcius.");
				System.out.println();
			}
			if(a==3)//??3?????
			{
				System.out.println("Good Bye");
				break;
			}
		}
	}
}

