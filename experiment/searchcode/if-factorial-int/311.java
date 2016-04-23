/*
 * The user enters a value of n on the console. On the console to print the last two digits 
 * (not including zero) in the sum of n !.
 */
import java.util.Scanner;

public class Task3th {

	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		System.out.println("Input number: ");
		int num = input.nextInt();
		int factorial = 1;
		int digit = 0;
		int newNumber = 0;
		
		for(int i = 1; i <= num; i++){
			factorial *= i;
			if(factorial > 99){
				digit = factorial % 10;
				if(digit == 0){
					while(factorial > 99){
					digit=factorial % 10;
					if(digit == 0)
				factorial /= 10;
					else
						break;
					}
				}
				else if (digit != 0){
					while(factorial > 9999){
						newNumber = factorial % 10;
						newNumber *= 10;
						factorial /= 10;
						newNumber += factorial % 10;
						factorial = newNumber % 10;
						factorial *= 10;
						newNumber /= 10;
						factorial += newNumber % 10;
					}
				}
			}		
		}
		if(factorial > 99){
			newNumber = factorial % 10;
			newNumber *= 10;
			factorial /= 10;
			newNumber += factorial % 10;
			factorial = newNumber % 10;
			factorial *= 10;
			newNumber /= 10;
			factorial += newNumber % 10;
		}
		System.out.println(factorial);
		input.close();
	}

}
