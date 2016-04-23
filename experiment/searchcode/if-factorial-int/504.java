import java.util.Scanner;
public class RecursiveFactorial {

	private static int factorial = 1;
	
	public static int factorial(int x){
		if(x > 0){
			factorial *= (x * factorial(x - 1));
		}
		return factorial;
	}
	
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		System.out.println("Enter an interger: ");
		int input = in.nextInt();
		System.out.println(input + "! = " + factorial(input));

	}

}

