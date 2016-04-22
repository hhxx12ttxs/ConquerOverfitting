import java.util.Scanner;
public class Task3 {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);

		
		System.out.print("Input number: ");
		int num = input.nextInt();
		int factorial = 1;
		int output = 0;

		
		for (int i = 1; i <= num; i++) {
			factorial *= i;
			while (factorial % 10 == 0) {
				factorial /= 10;
			}
			factorial %= 1000000;
		}

		
		output = factorial % 100;
		while (output < 10) {
			factorial /= 10;
			if (factorial == 0) {
			
				break;
			}
			output = (factorial % 10) * 10 + output;
		}

	
		System.out.println(output);

		input.close();
	}
}
	



