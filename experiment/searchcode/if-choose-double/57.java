package a1.s100502028;
import java.util.Scanner; // Using Scanner in the java.util package
public class A11 {
	// Main method
	public static void main(String[] args){
		Scanner input = new Scanner(System.in); // Create an object of the Scanner type
		
		// Prompt the user for name
		System.out.print("Please enter your name: ");
		String name = input.next(); 
		
		// Prompt the user for student number
		System.out.print("Please enter your student number: ");
		String number = input.next();
		
		System.out.println("Welcome to this easy calculator!!!");
		
		// Prompt the user for the initial number
		System.out.print("Input a number: ");
		double number1 = input.nextDouble();
		
		double result = 0;
		
		// While loop to do the program repeatedly
		while(true){
			// List the abilities and prompt the user for choice
			System.out.print("\n1.Add\n2.Minus\n3.Multiple\n4.Divide\n5.Show information\n6.Exit\n\n"
					+ "Choose a function to use: ");
			int choose = input.nextInt();
			
			// If statement for exit ability
			if(choose == 6){
				System.out.println(name + '(' + number + ')' + " Thanks for your using!!!");
				break;
			} //end if
			
			// Switch statement for choice of function
			switch (choose){
				case 1: // Case for add ability
					System.out.print("Input the number to add with the one you have input: ");
					double add = input.nextDouble();
					result = number1 + add;
					number1 = result;
					break;
				case 2: // Case for minus ability
					System.out.print("Input the number to minus with the one you have input: ");
					double minus = input.nextDouble();
					result = number1 - minus;
					number1 = result;
					break;
				case 3: // Case for multiple ability
					System.out.print("Input the number to multiple with the one you have input: ");
					double multiple = input.nextDouble();
					result = number1 * multiple;
					number1 = result;
					break;
				case 4: // Case for divide ability
					System.out.print("Input the number to divide with the one you have input: ");
					double divide = input.nextDouble();
					if(divide==0){ // If statement for non-exist condition
						System.out.println("Errors!!!Invalid input of divide!!!");
						System.exit(0);
					} // End if
					else{ // Else statement for correct condition
					result = number1 / divide;
					number1 = result;
					}
					break;
				case 5: // case for show information
					System.out.println("The result is: " + result);
					break;
				default: // Catch all other characters
					System.out.println("Errors!!!Invalid input of choose!!!");
					System.exit(0);
			} // End switch
		} // End while loop
	} // End main method
} //End class

