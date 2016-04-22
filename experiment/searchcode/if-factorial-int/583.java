/*
 * factorial of number 
 */
public class factorial {

	public static void main(String[] args){
		int num = 10;
		long fact = factorial.calcFactorial(num);
		if(fact == -1){
			System.out.print("The given number was less than ZERO");
		}
		System.out.print("Factorial of " + num + " is : " + fact);
	}
	
	public static long calcFactorial(int number) {
		if(number == 1 )
		{
			return 1;
		}
		if(number <= 0){
			return -1;
		}
		return calcFactorial(number-1)*number;
	}

}

