package practice;
import java.util.*;
public class Factorial {

	int result = 0;
	public int findFactorial(int num){
		
		if(num == 1){
			
			return 1;
		}
		else{
			
			result = num * findFactorial(num - 1);
			return result;
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Factorial fc = new Factorial();
		Scanner sc = new Scanner(System.in);
		int input_factorial = sc.nextInt();
		int factorial = fc.findFactorial(input_factorial);
		System.out.println("The factorial of: " + input_factorial + "  is:  " + factorial);
		sc.close();
	}

}

