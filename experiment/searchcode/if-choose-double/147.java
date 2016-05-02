import java.util.Scanner ;

public class Q1 {
	public static void main(String[] args){
		Scanner tem = new Scanner(System.in) ;
		
		double choose, result, a ;
		
		System.out.println("Please choose the method you want to use:") ;
		System.out.println("1.toFahrenheit") ;
		System.out.println("2.toCelcius") ;
		System.out.println("3.Exit ") ;
		
		choose = tem.nextDouble() ;//????????
		
		if( choose == 1 ){
			System.out.println("Please input the temperature:") ;
			a = tem.nextDouble() ;//????????
			result = a*9/5+32 ;
			System.out.println( a + " in Celcius is equal to " + result + " in Fahrenheit.") ;
		}
		
		if( choose == 2 ){
			System.out.println("Please input the temperature:") ;
			a = tem.nextDouble() ;//????????
			result = (a-32)*5/9 ;
			System.out.println( a + " in Fahrenheit is equal to "+ result +" in Celcius.") ;
		}
		
		if( choose == 3 ){
			System.out.println("Good Bye") ;
		}
	}
}

