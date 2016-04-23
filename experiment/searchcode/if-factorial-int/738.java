import java.util.Scanner;

public class Task4 {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		System.out.println("Enter two numbers for N! and K!. N must be bigger, otherwise the program will abort!");
		System.out.println("Enter a number for N!: ");
		int N = input.nextInt();
		
		System.out.println("Enter a number for K!: ");
		int K = input.nextInt();

		if (N < K) {
			System.out.println("N! is smaller than K!, the program will abort!");

		} else {
			long factorialN = 1;

			while (true) {

				if (N <= 1) {
					break;
				}
				
				factorialN = factorialN * N;
				N--;
			}

			long factorialK = 1;

			while (true) {
				if (K <= 1) {
					break;
				}

				factorialK = factorialK * K;
				K--;
			}
			System.out.println("N! is: " + factorialN);
			System.out.println("K! is: " + factorialK);
			
			long NK = factorialN * factorialK;
			double result = NK / (factorialN-factorialK);
			System.out.println("N!*K!/(N-K)! is: " + result);
			
		}
		input.close();
	}
}
