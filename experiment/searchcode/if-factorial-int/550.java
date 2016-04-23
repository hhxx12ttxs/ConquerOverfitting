import java.util.Scanner;

public class Factorial {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the number:");
		int num = sc.nextInt();
		
		factorialWithRecursion(num);
		
		sc.close();

	}
	
	public static void factorialWithRecursion(int num2){
		
		int factorial = 1;
		int i = 1;
		if(i<=num2){
			factorial *= i;
			factorialWithRecursion(num2);
		}
		
		System.out.println("The result is: " + factorial);
	}

}

