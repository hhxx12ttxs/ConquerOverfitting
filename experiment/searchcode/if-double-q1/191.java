package ce1002.s101502517;
import java.util.Scanner;
public class Q1 {
	public static void main(String[] args){
		Scanner input=new Scanner(System.in);
			double t=0;
		while(true){//??	
			int c=0;
			System.out.println("Please choose the method you want to use: ");
			System.out.println("1.toFahrenheit");
			System.out.println("2.toCelcius");
			System.out.println("3.Exit");
			c = input.nextInt();
			//?????????
			if(c==3){//????
				System.out.println("Good Bye");
				System.exit(0);
			}
			else{//????
				System.out.println("Please input the temperature:");
				t = input.nextDouble();
			}
			if(c==1){//ctof
				System.out.println( t +" in Celcius is equal to "+ctf(t)+" in Fahrenheit.");
				System.out.println();
			}
			else{//ftoc
				System.out.println( t+" in Fahrenheit is equal to "+ftc(t)+" in Celcius.");
				System.out.println();
			}
			
		}
		
	}
	
	public static double ftc(double f){//?????
		double c=(f-32)*5/9;
		return c;
	}
	public static double ctf(double c){//?????
		double f=c*9/5+32;
		return f;
	}
}

