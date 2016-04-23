import java.util.Scanner;

public class Factorial {
    public static void main(String[] args) {
	System.out.print("Choose number to calculate the factorial of: ");
	Scanner input = new Scanner(System.in);
	System.out.println(factorial(Integer.parseInt(input.next())));
    }

    // one liner!
    public static int factorial(int n) {
	return (n == 1 ? 1 : n * factorial(n - 1));
    }

    // origianl method
    // public static int factorial(int n) {
    // if (n == 1)
    // return 1;
    // else {
    // return n *= factorial(n - 1);
    // }
    // }

}

