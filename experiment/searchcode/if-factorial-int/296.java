import java.util.Scanner;

class RecursiveFactorial {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		RecursiveFactorial recursiveFactorialObj = new RecursiveFactorial();
		System.out.print("Factorial Finder!\nInput:");
		System.out.println("Result:"+recursiveFactorialObj.get_factorial(input.nextInt()));
	}
	int get_factorial(int data) {
		if(data!=1)
			return(data*get_factorial(data-1));
		return(1);
	}
}
